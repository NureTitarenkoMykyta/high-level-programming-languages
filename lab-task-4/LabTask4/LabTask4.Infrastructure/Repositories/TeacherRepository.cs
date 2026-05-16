using LabTask4.Domain;
using LanguageDuel.Infrastructure.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Infrastructure.Repositories;

public class TeacherRepository(IMongoDatabase database) : Repository<Teacher>(database)
{
    public async Task SyncNewTeacherAsync(Teacher newTeacher, List<string> courseIds, IClientSessionHandle session)
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
                    var teacherCourse = new TeacherCourse
                    {
                        TeacherId = newTeacher.Id,
                        CourseId = course.Id,
                        TeacherName = newTeacher.Name,
                        CourseName = course.Name
                    };

                    newTeacher.TeacherCourses.Add(teacherCourse);

                    var update = Builders<Course>.Update.Push(c => c.TeacherCourses, teacherCourse);
                    await courseCollection.UpdateOneAsync(session, c => c.Id == course.Id, update);
                }
            }
        }
    }
    
    public async Task SyncUpdatedTeacherCoursesAsync(Teacher updatedTeacher, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var courseFilter = Builders<Course>.Filter.ElemMatch(c => c.TeacherCourses, tc => tc.TeacherId == updatedTeacher.Id);
        var courseUpdate = Builders<Course>.Update.Set("TeacherCourses.$.TeacherName", updatedTeacher.Name);
        await courseCollection.UpdateManyAsync(session, courseFilter, courseUpdate);

        var currentTeacherInDb = await GetByIdAsync(MongoDB.Bson.ObjectId.Parse(updatedTeacher.Id));
        if (currentTeacherInDb == null) return;

        var oldCourseIds = currentTeacherInDb.TeacherCourses?.Select(tc => tc.CourseId).ToHashSet() ?? new HashSet<string>();
        var newCourseIds = updatedTeacher.TeacherCourses?.Select(tc => tc.CourseId).ToHashSet() ?? new HashSet<string>();

        var coursesToAdd = newCourseIds.Except(oldCourseIds).ToList();
        var coursesToRemove = oldCourseIds.Except(newCourseIds).ToList();

        if (coursesToAdd.Any())
        {
            var addFilter = Builders<Course>.Filter.In(c => c.Id, coursesToAdd);
            var addUpdate = Builders<Course>.Update.Push(c => c.TeacherCourses, new TeacherCourse
            {
                TeacherId = updatedTeacher.Id,
                CourseId = null,
                TeacherName = updatedTeacher.Name,
                CourseName = null
            });
            await courseCollection.UpdateManyAsync(session, addFilter, addUpdate);
        }

        if (coursesToRemove.Any())
        {
            var removeFilter = Builders<Course>.Filter.In(c => c.Id, coursesToRemove);
            var removeUpdate = Builders<Course>.Update.PullFilter(c => c.TeacherCourses, tc => tc.TeacherId == updatedTeacher.Id);
            await courseCollection.UpdateManyAsync(session, removeFilter, removeUpdate);
        }
    }

    public async Task SyncUpdatedTeacherToLessonsAsync(Teacher updatedTeacher, IClientSessionHandle session)
    {
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");
        var filter = Builders<Lesson>.Filter.Eq(l => l.TeacherId, updatedTeacher.Id);
        var update = Builders<Lesson>.Update.Set("TeacherName", updatedTeacher.Name); 
        await lessonCollection.UpdateManyAsync(session, filter, update);
    }

    public async Task SyncDeletedTeacherAsync(string teacherId, IClientSessionHandle session)
    {
        var courseCollection = Database.GetCollection<Course>("Courses");

        var courseFilter = Builders<Course>.Filter.ElemMatch(c => c.TeacherCourses, tc => tc.TeacherId == teacherId);
        var courseUpdate = Builders<Course>.Update.PullFilter(c => c.TeacherCourses, tc => tc.TeacherId == teacherId);

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