using LabTask4.Domain;
using LanguageDuel.Infrastructure.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Infrastructure.Repositories;

public class StudentRepository(IMongoDatabase database) : Repository<Student>(database)
{
    public async Task SyncNewStudentAsync(Student newStudent, List<string> courseIds, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        if (courseIds != null && courseIds.Count > 0)
        {
            foreach (var courseId in courseIds)
            {
                if (!ObjectId.TryParse(courseId, out var courseObjectId)) continue;

                var course = await courseCollection.Find(session, c => c.Id == courseId).FirstOrDefaultAsync();
                if (course != null)
                {
                    var studentCourse = new StudentCourse
                    {
                        StudentId = newStudent.Id,
                        CourseId = course.Id,
                        StudentName = newStudent.Name,
                        CourseName = course.Name
                    };

                    newStudent.StudentCourses.Add(studentCourse);

                    var update = Builders<Course>.Update.Push(c => c.StudentCourses, studentCourse);
                    await courseCollection.UpdateOneAsync(session, c => c.Id == course.Id, update);
                }
            }
        }
    }
    
    public async Task SyncUpdatedStudentAsync(Student updatedStudent, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var courseFilter = Builders<Course>.Filter.ElemMatch(c => c.StudentCourses, sc => sc.StudentId == updatedStudent.Id);
        var courseUpdate = Builders<Course>.Update.Set("StudentCourses.$.StudentName", updatedStudent.Name);
        await courseCollection.UpdateManyAsync(session, courseFilter, courseUpdate);

        var currentStudentInDb = await GetByIdAsync(MongoDB.Bson.ObjectId.Parse(updatedStudent.Id));
        if (currentStudentInDb == null) return;

        var oldCourseIds = currentStudentInDb.StudentCourses?.Select(sc => sc.CourseId).ToHashSet() ?? new HashSet<string>();
        var newCourseIds = updatedStudent.StudentCourses?.Select(sc => sc.CourseId).ToHashSet() ?? new HashSet<string>();

        var coursesToAdd = newCourseIds.Except(oldCourseIds).ToList();
        var coursesToRemove = oldCourseIds.Except(newCourseIds).ToList();

        if (coursesToAdd.Any())
        {
            var addFilter = Builders<Course>.Filter.In(c => c.Id, coursesToAdd);
            var addUpdate = Builders<Course>.Update.Push(c => c.StudentCourses, new StudentCourse
            {
                StudentId = updatedStudent.Id,
                CourseId = null,
                StudentName = updatedStudent.Name,
                CourseName = null
            });
            await courseCollection.UpdateManyAsync(session, addFilter, addUpdate);
        }

        if (coursesToRemove.Any())
        {
            var removeFilter = Builders<Course>.Filter.In(c => c.Id, coursesToRemove);
            var removeUpdate = Builders<Course>.Update.PullFilter(c => c.StudentCourses, sc => sc.StudentId == updatedStudent.Id);
            await courseCollection.UpdateManyAsync(session, removeFilter, removeUpdate);
        }
    }

    public async Task SyncDeletedStudentAsync(string studentId, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var courseFilter = Builders<Course>.Filter.ElemMatch(c => c.StudentCourses, sc => sc.StudentId == studentId);
        var courseUpdate = Builders<Course>.Update.PullFilter(c => c.StudentCourses, sc => sc.StudentId == studentId);

        await courseCollection.UpdateManyAsync(session, courseFilter, courseUpdate);
    }
    
    public async Task ValidateCoursesExistAsync(List<string> courseIds, IClientSessionHandle session)
    {
        if (courseIds == null || courseIds.Count == 0) return;

        var courseCollection = Database.GetCollection<Course>("Courses");

        foreach (var id in courseIds)
        {
            if (!ObjectId.TryParse(id, out _))
            {
                throw new ArgumentException($"Invalid Course ID format: {id}");
            }

            var exists = await courseCollection.Find(session, c => c.Id == id).AnyAsync();
            if (!exists)
            {
                throw new KeyNotFoundException($"Course with ID {id} not found.");
            }
        }
    }
}