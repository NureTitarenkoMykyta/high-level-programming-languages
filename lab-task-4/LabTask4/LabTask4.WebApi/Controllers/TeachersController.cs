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
public class TeachersController(
    TeacherRepository teacherRepository, 
    IRepository<Course> courseRepository,
    IUnitOfWork uow, 
    IMapper mapper) : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<IActionResult> Get(string id)
    {
        if (!ObjectId.TryParse(id, out var teacherObjectId))
        {
            return BadRequest(new { message = "Invalid Teacher ID format" });
        }
        var teacher = await teacherRepository.GetByIdAsync(teacherObjectId);
        if (teacher == null)
        {
            return NotFound(new { message = "Teacher not found" });
        }
        return Ok(teacher);
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var teachers = await teacherRepository.GetAllAsync();
        return Ok(teachers);
    }

    [HttpPost]
    [EvictCache("all")]
    public async Task<IActionResult> Create(TeacherDto dto)
    {
        var session = await uow.StartTransactionAsync();
        try
        {
            await teacherRepository.ValidateCoursesExistAsync(dto.CourseIds, session);
    
            var teacher = mapper.Map<Teacher>(dto);
            teacher.Id = ObjectId.GenerateNewId().ToString();
            teacher.TeacherCourses = new List<TeacherCourse>();
    
            await teacherRepository.SyncNewTeacherAsync(teacher, dto.CourseIds, session);
            await teacherRepository.AddAsync(teacher, session);
    
            await uow.CommitAsync();
            return CreatedAtAction(nameof(Get), new { id = teacher.Id }, teacher);
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
    public async Task<IActionResult> Update(string id, TeacherDto dto)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return BadRequest(new { message = "Invalid Teacher ID format" });
        }
    
        var session = await uow.StartTransactionAsync();
        try
        {
            await teacherRepository.ValidateCoursesExistAsync(dto.CourseIds, session);
    
            var teacher = mapper.Map<Teacher>(dto);
            teacher.Id = id;
            teacher.TeacherCourses = new List<TeacherCourse>();
    
            if (dto.CourseIds != null)
            {
                foreach (var courseId in dto.CourseIds)
                {
                    if (!ObjectId.TryParse(courseId, out var courseObjectId)) continue;
                    var course = await courseRepository.GetByIdAsync(courseObjectId);
                    if (course != null)
                    {
                        teacher.TeacherCourses.Add(new TeacherCourse
                        {
                            TeacherId = teacher.Id,
                            CourseId = course.Id,
                            TeacherName = teacher.Name,
                            CourseName = course.Name
                        });
                    }
                }
            }
    
            await teacherRepository.SyncUpdatedTeacherCoursesAsync(teacher, session);
            await teacherRepository.SyncUpdatedTeacherToLessonsAsync(teacher, session);
    
            var isUpdated = await teacherRepository.UpdateAsync(teacher, session);
            if (!isUpdated)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Teacher not found" });
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
        if (!ObjectId.TryParse(id, out var teacherObjectId))
        {
            return BadRequest(new { message = "Invalid Teacher ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            var isDeleted = await teacherRepository.DeleteAsync(teacherObjectId, session);
            if (!isDeleted)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Teacher not found" });
            }

            await teacherRepository.SyncDeletedTeacherAsync(id, session);

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