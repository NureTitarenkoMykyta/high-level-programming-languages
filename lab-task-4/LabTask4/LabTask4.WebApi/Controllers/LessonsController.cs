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
public class LessonsController(
    LessonRepository lessonRepository, 
    IUnitOfWork uow, 
    IMapper mapper) : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<IActionResult> Get(string id)
    {
        if (!ObjectId.TryParse(id, out var lessonObjectId))
        {
            return BadRequest(new { message = "Invalid Lesson ID format" });
        }
        var lesson = await lessonRepository.GetByIdAsync(lessonObjectId);
        if (lesson == null)
        {
            return NotFound(new { message = "Lesson not found" });
        }
        return Ok(lesson);
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var lessons = await lessonRepository.GetAllAsync();
        return Ok(lessons);
    }

    [HttpPost]
    [EvictCache("all")]
    public async Task<IActionResult> Create(LessonDto dto)
    {
        var session = await uow.StartTransactionAsync();
        try
        {
            await lessonRepository.ValidateEntitiesExistAsync(dto.CourseId, dto.TeacherId, session);

            var lesson = mapper.Map<Lesson>(dto);
            lesson.Id = ObjectId.GenerateNewId().ToString();

            await lessonRepository.AddAsync(lesson, session);
            
            await uow.CommitAsync();
            return CreatedAtAction(nameof(Get), new { id = lesson.Id }, lesson);
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
    public async Task<IActionResult> Update(string id, LessonDto dto)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return BadRequest(new { message = "Invalid Lesson ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            await lessonRepository.ValidateEntitiesExistAsync(dto.CourseId, dto.TeacherId, session);

            var lesson = mapper.Map<Lesson>(dto);
            lesson.Id = id;

            var isUpdated = await lessonRepository.UpdateAsync(lesson, session);
            if (!isUpdated)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Lesson not found" });
            }

            await lessonRepository.SyncUpdatedLessonAsync(lesson, session);

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
        if (!ObjectId.TryParse(id, out var lessonObjectId))
        {
            return BadRequest(new { message = "Invalid Lesson ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            var lesson = await lessonRepository.GetByIdAsync(lessonObjectId);
            if (lesson == null)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Lesson not found" });
            }

            var isDeleted = await lessonRepository.DeleteAsync(lessonObjectId, session);
            if (!isDeleted)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Lesson not found" });
            }

            await lessonRepository.RemoveGradesLinkedToLessonAsync(id, session);
            await lessonRepository.SyncDeletedLessonAsync(lesson.CourseId, id, session);

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