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
public class CoursesController(
    CourseRepository courseRepository, 
    IUnitOfWork uow, 
    IMapper mapper) : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<IActionResult> Get(string id)
    {
        if (!ObjectId.TryParse(id, out var courseObjectId))
        {
            return BadRequest(new { message = "Invalid Course ID format" });
        }
        var course = await courseRepository.GetByIdAsync(courseObjectId);
        if (course == null)
        {
            return NotFound(new { message = "Course not found" });
        }
        return Ok(course);
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var courses = await courseRepository.GetAllAsync();
        return Ok(courses);
    }

    [HttpPost]
    [EvictCache("all")]
    public async Task<IActionResult> Create(CourseDto dto)
    {
        var session = await uow.StartTransactionAsync();
        try
        {
            await courseRepository.ValidateEntitiesExistAsync(dto.StudentIds, dto.TeacherIds, session);

            var course = mapper.Map<Course>(dto);
            course.Id = ObjectId.GenerateNewId().ToString();
            course.Lessons = new List<Lesson>();
            course.StudentCourses = new List<StudentCourse>();
            course.TeacherCourses = new List<TeacherCourse>();

            await courseRepository.SyncNewCourseAsync(course, dto.StudentIds, dto.TeacherIds, session);
            await courseRepository.AddAsync(course, session);

            await uow.CommitAsync();
            return CreatedAtAction(nameof(Get), new { id = course.Id }, course);
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
    public async Task<IActionResult> Update(string id, CourseDto dto)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return BadRequest(new { message = "Invalid Course ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            await courseRepository.ValidateEntitiesExistAsync(dto.StudentIds, dto.TeacherIds, session);

            var course = mapper.Map<Course>(dto);
            course.Id = id;

            await courseRepository.SyncUpdatedCourseAsync(course, session);

            var isUpdated = await courseRepository.UpdateAsync(course, session);
            if (!isUpdated)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Course not found" });
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
        if (!ObjectId.TryParse(id, out var courseObjectId))
        {
            return BadRequest(new { message = "Invalid Course ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            var isDeleted = await courseRepository.DeleteAsync(courseObjectId, session);
            if (!isDeleted)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Course not found" });
            }

            await courseRepository.SyncDeletedCourseAsync(id, session);

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