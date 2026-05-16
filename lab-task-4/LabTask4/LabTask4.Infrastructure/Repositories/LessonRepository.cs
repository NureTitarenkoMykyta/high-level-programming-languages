using LabTask4.Domain;
using LanguageDuel.Infrastructure.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Infrastructure.Repositories;

public class LessonRepository(IMongoDatabase database) : Repository<Lesson>(database)
{
    public async Task RemoveGradesLinkedToLessonAsync(string lessonId, IClientSessionHandle session)
    {
        var gradeCollection = Database.GetCollection<Grade>("Grades");
        var filter = Builders<Grade>.Filter.Eq(g => g.LessonId, lessonId);
        
        await gradeCollection.DeleteManyAsync(session, filter);
    }

    public async Task SyncUpdatedLessonAsync(Lesson updatedLesson, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var filter = Builders<Course>.Filter.And(
            Builders<Course>.Filter.Eq(c => c.Id, updatedLesson.CourseId),
            Builders<Course>.Filter.ElemMatch(c => c.Lessons, l => l.Id == updatedLesson.Id)
        );
        var update = Builders<Course>.Update.Set("Lessons.$", updatedLesson);

        await courseCollection.UpdateOneAsync(session, filter, update);
    }

    public async Task SyncDeletedLessonAsync(string courseId, string lessonId, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var filter = Builders<Course>.Filter.Eq(c => c.Id, courseId);
        var update = Builders<Course>.Update.PullFilter(c => c.Lessons, l => l.Id == lessonId);

        await courseCollection.UpdateOneAsync(session, filter, update);
    }
    
    public async Task ValidateEntitiesExistAsync(string courseId, string teacherId, IClientSessionHandle session)
    {
        if (!ObjectId.TryParse(courseId, out _))
        {
            throw new ArgumentException($"Invalid Course ID format: {courseId}");
        }

        if (!ObjectId.TryParse(teacherId, out _))
        {
            throw new ArgumentException($"Invalid Teacher ID format: {teacherId}");
        }

        var courseCollection = Database.GetCollection<Course>("Courses");
        var teacherCollection = Database.GetCollection<Teacher>("Teachers");

        var courseExists = await courseCollection.Find(session, c => c.Id == courseId).AnyAsync();
        if (!courseExists)
        {
            throw new KeyNotFoundException($"Course with ID {courseId} not found.");
        }

        var teacherExists = await teacherCollection.Find(session, t => t.Id == teacherId).AnyAsync();
        if (!teacherExists)
        {
            throw new KeyNotFoundException($"Teacher with ID {teacherId} not found.");
        }
    }
}