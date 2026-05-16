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
public class GradesController(
    GradeRepository gradeRepository, 
    IUnitOfWork uow, 
    IMapper mapper) : ControllerBase
{
    [HttpGet("{id}")]
    public async Task<IActionResult> Get(string id)
    {
        if (!ObjectId.TryParse(id, out var gradeObjectId))
        {
            return BadRequest(new { message = "Invalid Grade ID format" });
        }
        var grade = await gradeRepository.GetByIdAsync(gradeObjectId);
        if (grade == null)
        {
            return NotFound(new { message = "Grade not found" });
        }
        return Ok(grade);
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var grades = await gradeRepository.GetAllAsync();
        return Ok(grades);
    }

    [HttpPost]
    [EvictCache("all")]
    public async Task<IActionResult> Create(GradeDto dto)
    {
        var session = await uow.StartTransactionAsync();
        try
        {
            await gradeRepository.ValidateEntitiesExistAsync(dto.StudentId, dto.LessonId, session);

            var grade = mapper.Map<Grade>(dto);
            grade.Id = ObjectId.GenerateNewId().ToString();

            await gradeRepository.AddAsync(grade, session);
            await gradeRepository.PushGradeToLessonAsync(grade, session);

            await uow.CommitAsync();
            return CreatedAtAction(nameof(Get), new { id = grade.Id }, grade);
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
    public async Task<IActionResult> Update(string id, GradeDto dto)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return BadRequest(new { message = "Invalid Grade ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            await gradeRepository.ValidateEntitiesExistAsync(dto.StudentId, dto.LessonId, session);

            var grade = mapper.Map<Grade>(dto);
            grade.Id = id;

            var isUpdated = await gradeRepository.UpdateAsync(grade, session);
            if (!isUpdated)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Grade not found" });
            }

            await gradeRepository.SyncUpdatedGradeInLessonAsync(grade, session);
            
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
        if (!ObjectId.TryParse(id, out var gradeObjectId))
        {
            return BadRequest(new { message = "Invalid Grade ID format" });
        }

        var session = await uow.StartTransactionAsync();
        try
        {
            var grade = await gradeRepository.GetByIdAsync(gradeObjectId);
            if (grade == null)
            {
                await uow.AbortAsync();
                return NotFound(new { message = "Grade not found" });
            }

            await gradeRepository.DeleteAsync(gradeObjectId, session);
            await gradeRepository.PullGradeFromLessonAsync(grade, session);

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