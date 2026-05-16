using AutoMapper;
using LabTask4.Application.Repositories;
using LabTask4.Attributes;
using LabTask4.Domain;
using LabTask4.Dtos;
using LabTask4.Infrastructure.Repositories;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.OutputCaching;
using MongoDB.Bson;

namespace LabTask4.Controllers;

[ApiController]
[Route("api/[controller]")]
[OutputCache(Tags = ["all"])]
public class StudentsController(
    StudentRepository studentRepository, 
    IRepository<Course> courseRepository,
    IUnitOfWork uow, 
    IMapper mapper) : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<IActionResult> Get(string id)
    {
        if (!ObjectId.TryParse(id, out var studentObjectId))
        {
            return BadRequest(new { message = "Invalid Student ID format" });
        }

        var student = await studentRepository.GetByIdAsync(studentObjectId);
        if (student == null)
        {
            return NotFound(new { message = "Student not found" });
        }

        return Ok(student);
    }
    
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var students = await studentRepository.GetAllAsync();
        return Ok(students);
    }
    
    [HttpPost]
    [EvictCache("all")]
    public async Task<IActionResult> Create(StudentDto dto)
    {
        var session = await uow.StartTransactionAsync();
        try
        {
            await studentRepository.ValidateCoursesExistAsync(dto.CourseIds, session);
    
            var student = mapper.Map<Student>(dto);
            student.Id = ObjectId.GenerateNewId().ToString();
            student.StudentCourses = new List<StudentCourse>();
    
            await studentRepository.SyncNewStudentAsync(student, dto.CourseIds, session);
            await studentRepository.AddAsync(student, session);
            
            await uow.CommitAsync();
            return CreatedAtAction(nameof(Get), new { id = student.Id }, student);
        }
        catch (ArgumentException ex)
        {
            await uow.AbortAsync();
            return BadRequest(new { message = ex.Message });
        }
        catch (KeyNotFoundException ex)
        {
            await uow.AbortAsync();
            return NotFound(new { message = ex.Message });
        }
        catch
        {
            await uow.AbortAsync();
            throw;
        }
    }
    
    [HttpPut("{id}")]
    [EvictCache("all")]
    public async Task<IActionResult> Update(string id, StudentDto dto)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return BadRequest(new { message = "Invalid Student ID format" });
        }
    
        var session = await uow.StartTransactionAsync();
        try
        {
            await studentRepository.ValidateCoursesExistAsync(dto.CourseIds, session);
    
            var student = mapper.Map<Student>(dto);
            student.Id = id;
            student.StudentCourses = new List<StudentCourse>();
    
            if (dto.CourseIds != null)
            {
                foreach (var courseId in dto.CourseIds)
                {
                    if (!ObjectId.TryParse(courseId, out var courseObjectId)) continue;
                    var course = await courseRepository.GetByIdAsync(courseObjectId);
                    if (course != null)
                    {
                        student.StudentCourses.Add(new StudentCourse
                        {
                            StudentId = student.Id,
                            CourseId = course.Id,
                            StudentName = student.Name,
                            CourseName = course.Name
                        });
                    }
                }
            }
    
            await studentRepository.SyncUpdatedStudentAsync(student, session);
    
            var isUpdated = await studentRepository.UpdateAsync(student, session);
            if (!isUpdated)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Student not found" });
            }
            
            await uow.CommitAsync();
            return NoContent();
        }
        catch (ArgumentException ex)
        {
            await uow.AbortAsync();
            return BadRequest(new { message = ex.Message });
        }
        catch (KeyNotFoundException ex)
        {
            await uow.AbortAsync();
            return NotFound(new { message = ex.Message });
        }
        catch
        {
            await uow.AbortAsync();
            throw;
        }
    }
    
    [HttpDelete("{id}")]
    [EvictCache("all")]
    public async Task<IActionResult> Delete(string id)
    {
        if (!ObjectId.TryParse(id, out var studentObjectId))
        {
            return BadRequest(new { message = "Invalid Student ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            var isDeleted = await studentRepository.DeleteAsync(studentObjectId, session);
            if (!isDeleted)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Student not found" });
            }

            await studentRepository.SyncDeletedStudentAsync(id, session);

            await uow.CommitAsync();
            return NoContent();
        }
        catch
        {
            await uow.AbortAsync();
            throw;
        }
    }
}