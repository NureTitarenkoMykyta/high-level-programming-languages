using LabTask4.Domain;
using LanguageDuel.Infrastructure.Repositories;
using MongoDB.Bson;
using MongoDB.Driver;

namespace LabTask4.Infrastructure.Repositories;

public class CourseRepository(IMongoDatabase database) : Repository<Course>(database)
{
    public async Task SyncNewCourseAsync(Course newCourse, List<string> studentIds, List<string> teacherIds, IClientSessionHandle session)
    {
        var studentCollection = Database.GetCollection<Student>("Students");
        var teacherCollection = Database.GetCollection<Teacher>("Teachers");
    
        if (studentIds != null && studentIds.Count > 0)
        {
            foreach (var studentId in studentIds)
            {
                if (!ObjectId.TryParse(studentId, out var studentObjectId)) continue;
                
                var student = await studentCollection.Find(session, s => s.Id == studentId).FirstOrDefaultAsync();
                if (student != null)
                {
                    var studentCourse = new StudentCourse
                    {
                        StudentId = student.Id,
                        CourseId = newCourse.Id,
                        StudentName = student.Name,
                        CourseName = newCourse.Name
                    };
                    
                    newCourse.StudentCourses.Add(studentCourse);
                    
                    var update = Builders<Student>.Update.Push(s => s.StudentCourses, studentCourse);
                    await studentCollection.UpdateOneAsync(session, s => s.Id == student.Id, update);
                }
            }
        }
    
        if (teacherIds != null && teacherIds.Count > 0)
        {
            foreach (var teacherId in teacherIds)
            {
                if (!ObjectId.TryParse(teacherId, out var teacherObjectId)) continue;
                
                var teacher = await teacherCollection.Find(session, t => t.Id == teacherId).FirstOrDefaultAsync();
                if (teacher != null)
                {
                    var teacherCourse = new TeacherCourse
                    {
                        TeacherId = teacher.Id,
                        CourseId = newCourse.Id,
                        TeacherName = teacher.Name,
                        CourseName = newCourse.Name
                    };
                    
                    newCourse.TeacherCourses.Add(teacherCourse);
                    
                    var update = Builders<Teacher>.Update.Push(t => t.TeacherCourses, teacherCourse);
                    await teacherCollection.UpdateOneAsync(session, t => t.Id == teacher.Id, update);
                }
            }
        }
    }
    
    public async Task SyncUpdatedCourseAsync(Course updatedCourse, IClientSessionHandle session)
    {
        var studentCollection = Database.GetCollection<Student>("Students");
        var teacherCollection = Database.GetCollection<Teacher>("Teachers");

        var studentFilter = Builders<Student>.Filter.ElemMatch(s => s.StudentCourses, sc => sc.CourseId == updatedCourse.Id);
        var studentUpdate = Builders<Student>.Update.Set("StudentCourses.$.CourseName", updatedCourse.Name);
        await studentCollection.UpdateManyAsync(session, studentFilter, studentUpdate);

        var teacherFilter = Builders<Teacher>.Filter.ElemMatch(t => t.TeacherCourses, tc => tc.CourseId == updatedCourse.Id);
        var teacherUpdate = Builders<Teacher>.Update.Set("TeacherCourses.$.CourseName", updatedCourse.Name);
        await teacherCollection.UpdateManyAsync(session, teacherFilter, teacherUpdate);

        var currentCourseInDb = await GetByIdAsync(MongoDB.Bson.ObjectId.Parse(updatedCourse.Id));
        if (currentCourseInDb == null) return;

        var oldStudentIds = currentCourseInDb.StudentCourses?.Select(sc => sc.StudentId).ToHashSet() ?? new HashSet<string>();
        var newStudentIds = updatedCourse.StudentCourses?.Select(sc => sc.StudentId).ToHashSet() ?? new HashSet<string>();

        var studentsToAdd = newStudentIds.Except(oldStudentIds).ToList();
        var studentsToRemove = oldStudentIds.Except(newStudentIds).ToList();

        if (studentsToAdd.Any())
        {
            var addFilter = Builders<Student>.Filter.In(s => s.Id, studentsToAdd);
            var addUpdate = Builders<Student>.Update.Push(s => s.StudentCourses, new StudentCourse
            {
                StudentId = null, 
                CourseId = updatedCourse.Id,
                StudentName = null, 
                CourseName = updatedCourse.Name
            });
            await studentCollection.UpdateManyAsync(session, addFilter, addUpdate);
        }

        if (studentsToRemove.Any())
        {
            var removeFilter = Builders<Student>.Filter.In(s => s.Id, studentsToRemove);
            var removeUpdate = Builders<Student>.Update.PullFilter(s => s.StudentCourses, sc => sc.CourseId == updatedCourse.Id);
            await studentCollection.UpdateManyAsync(session, removeFilter, removeUpdate);
        }

        var oldTeacherIds = currentCourseInDb.TeacherCourses?.Select(tc => tc.TeacherId).ToHashSet() ?? new HashSet<string>();
        var newTeacherIds = updatedCourse.TeacherCourses?.Select(tc => tc.TeacherId).ToHashSet() ?? new HashSet<string>();

        var teachersToAdd = newTeacherIds.Except(oldTeacherIds).ToList();
        var teachersToRemove = oldTeacherIds.Except(newTeacherIds).ToList();

        if (teachersToAdd.Any())
        {
            var addFilter = Builders<Teacher>.Filter.In(t => t.Id, teachersToAdd);
            var addUpdate = Builders<Teacher>.Update.Push(t => t.TeacherCourses, new TeacherCourse
            {
                TeacherId = null,
                CourseId = updatedCourse.Id,
                TeacherName = null,
                CourseName = updatedCourse.Name
            });
            await teacherCollection.UpdateManyAsync(session, addFilter, addUpdate);
        }

        if (teachersToRemove.Any())
        {
            var removeFilter = Builders<Teacher>.Filter.In(t => t.Id, teachersToRemove);
            var removeUpdate = Builders<Teacher>.Update.PullFilter(t => t.TeacherCourses, tc => tc.CourseId == updatedCourse.Id);
            await teacherCollection.UpdateManyAsync(session, removeFilter, removeUpdate);
        }
    }

    public async Task SyncDeletedCourseAsync(string courseId, IClientSessionHandle session)
    {
        var studentCollection = Database.GetCollection<Student>("Students");
        var teacherCollection = Database.GetCollection<Teacher>("Teachers");
        var lessonCollection = Database.GetCollection<Lesson>("Lessons");
        var gradeCollection = Database.GetCollection<Grade>("Grades");

        var studentFilter = Builders<Student>.Filter.ElemMatch(s => s.StudentCourses, sc => sc.CourseId == courseId);
        var studentUpdate = Builders<Student>.Update.PullFilter(s => s.StudentCourses, sc => sc.CourseId == courseId);

        var teacherFilter = Builders<Teacher>.Filter.ElemMatch(t => t.TeacherCourses, tc => tc.CourseId == courseId);
        var teacherUpdate = Builders<Teacher>.Update.PullFilter(t => t.TeacherCourses, tc => tc.CourseId == courseId);

        await studentCollection.UpdateManyAsync(session, studentFilter, studentUpdate);
        await teacherCollection.UpdateManyAsync(session, teacherFilter, teacherUpdate);

        var courseLessons = await lessonCollection
            .Find(session, l => l.CourseId == courseId)
            .Project(l => l.Id)
            .ToListAsync();

        if (courseLessons.Any())
        {
            var gradesFilter = Builders<Grade>.Filter.In(g => g.LessonId, courseLessons);
            await gradeCollection.DeleteManyAsync(session, gradesFilter);

            var lessonsFilter = Builders<Lesson>.Filter.Eq(l => l.CourseId, courseId);
            await lessonCollection.DeleteManyAsync(session, lessonsFilter);
        }
    }
    
    public async Task ValidateEntitiesExistAsync(List<string> studentIds, List<string> teacherIds, IClientSessionHandle session)
    {
        var studentCollection = Database.GetCollection<Student>("Students");
        var teacherCollection = Database.GetCollection<Teacher>("Teachers");

        if (studentIds != null && studentIds.Count > 0)
        {
            foreach (var id in studentIds)
            {
                if (!ObjectId.TryParse(id, out _))
                {
                    throw new ArgumentException($"Invalid Student ID format: {id}");
                }

                var exists = await studentCollection.Find(session, s => s.Id == id).AnyAsync();
                if (!exists)
                {
                    throw new KeyNotFoundException($"Student with ID {id} not found.");
                }
            }
        }

        if (teacherIds != null && teacherIds.Count > 0)
        {
            foreach (var id in teacherIds)
            {
                if (!ObjectId.TryParse(id, out _))
                {
                    throw new ArgumentException($"Invalid Teacher ID format: {id}");
                }

                var exists = await teacherCollection.Find(session, t => t.Id == id).AnyAsync();
                if (!exists)
                {
                    throw new KeyNotFoundException($"Teacher with ID {id} not found.");
                }
            }
        }
    }
}