using LabTask4.Domain;
using LanguageDuel.Infrastructure.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Infrastructure.Repositories;

public class GradeRepository(IMongoDatabase database) : Repository<Grade>(database)
{
    public async Task PushGradeToLessonAsync(Grade grade, IClientSessionHandle session)
    {
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");
        var filter = Builders<Lesson>.Filter.Eq(l => l.Id, grade.LessonId);
        var update = Builders<Lesson>.Update.Push(l => l.Grades, grade);

        await lessonCollection.UpdateOneAsync(session, filter, update);
    }

    public async Task SyncUpdatedGradeInLessonAsync(Grade updatedGrade, IClientSessionHandle session)
    {
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");

        var filter = Builders<Lesson>.Filter.And(
            Builders<Lesson>.Filter.Eq(l => l.Id, updatedGrade.LessonId),
            Builders<Lesson>.Filter.ElemMatch(l => l.Grades, g => g.Id == updatedGrade.Id)
        );
        var update = Builders<Lesson>.Update.Set("Grades.$", updatedGrade);

        await lessonCollection.UpdateOneAsync(session, filter, update);
    }

    public async Task PullGradeFromLessonAsync(Grade grade, IClientSessionHandle session)
    {
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");
        
        var filter = Builders<Lesson>.Filter.Eq(l => l.Id, grade.LessonId);
        var update = Builders<Lesson>.Update.PullFilter(l => l.Grades, g => g.Id == grade.Id);

        await lessonCollection.UpdateOneAsync(session, filter, update);
    }
    
    public async Task ValidateEntitiesExistAsync(string studentId, string lessonId, IClientSessionHandle session)
    {
        if (!ObjectId.TryParse(studentId, out _))
        {
            throw new ArgumentException($"Invalid Student ID format: {studentId}");
        }

        if (!ObjectId.TryParse(lessonId, out _))
        {
            throw new ArgumentException($"Invalid Lesson ID format: {lessonId}");
        }

        var studentCollection = Database.GetCollection<Student>("Students");
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");

        var studentExists = await studentCollection.Find(session, s => s.Id == studentId).AnyAsync();
        if (!studentExists)
        {
            throw new KeyNotFoundException($"Student with ID {studentId} not found.");
        }

        var lessonExists = await lessonCollection.Find(session, l => l.Id == lessonId).AnyAsync();
        if (!lessonExists)
        {
            throw new KeyNotFoundException($"Lesson with ID {lessonId} not found.");
        }
    }
}