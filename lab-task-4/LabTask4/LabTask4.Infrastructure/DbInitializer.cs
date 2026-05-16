using LabTask4.Domain;
using MongoDB.Driver;

namespace LabTask4.Infrastructure;

public static class DbInitializer
{
    public static async Task CreateInitialDataAsync(IMongoDatabase database)
    {
        var courseCollection = database.GetCollection<Course>("Courses");
        var studentCollection = database.GetCollection<Student>("Students");
        var teacherCollection = database.GetCollection<Teacher>("Teachers");
        var gradeCollection = database.GetCollection<Grade>("Grades");
        var lessonCollection = database.GetCollection<Lesson>("Lessons");

        var courseCount = await courseCollection.CountDocumentsAsync(_ => true);
        if (courseCount >= 3)
        {
            return;
        }

        await database.DropCollectionAsync("Courses");
        await database.DropCollectionAsync("Students");
        await database.DropCollectionAsync("Teachers");
        await database.DropCollectionAsync("Grades");
        await database.DropCollectionAsync("Lessons");

        var teachers = new List<Teacher>
        {
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "John Doe", Email = "john.doe@university.com", DateOfBirth = new DateTime(1980, 5, 14, 0, 0, 0, DateTimeKind.Utc), TeacherCourses = new List<TeacherCourse>() },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Jane Smith", Email = "jane.smith@university.com", DateOfBirth = new DateTime(1985, 10, 22, 0, 0, 0, DateTimeKind.Utc), TeacherCourses = new List<TeacherCourse>() }
        };
        await teacherCollection.InsertManyAsync(teachers);

        var students = new List<Student>
        {
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Alice Johnson", Email = "alice@gmail.com", Age = 20, DateOfBirth = new DateTime(2006, 3, 15, 0, 0, 0, DateTimeKind.Utc), StudentCourses = new List<StudentCourse>() },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Bob Miller", Email = "bob@gmail.com", Age = 21, DateOfBirth = new DateTime(2005, 7, 19, 0, 0, 0, DateTimeKind.Utc), StudentCourses = new List<StudentCourse>() },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Charlie Brown", Email = "charlie@gmail.com", Age = 19, DateOfBirth = new DateTime(2007, 11, 2, 0, 0, 0, DateTimeKind.Utc), StudentCourses = new List<StudentCourse>() }
        };
        await studentCollection.InsertManyAsync(students);

        var courses = new List<Course>
        {
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Introduction to Computer Science", Code = "CS101", Lessons = new List<Lesson>(), StudentCourses = new List<StudentCourse>(), TeacherCourses = new List<TeacherCourse>() },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Advanced Mathematics", Code = "MATH301", Lessons = new List<Lesson>(), StudentCourses = new List<StudentCourse>(), TeacherCourses = new List<TeacherCourse>() },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), Name = "Database Management Systems", Code = "CS202", Lessons = new List<Lesson>(), StudentCourses = new List<StudentCourse>(), TeacherCourses = new List<TeacherCourse>() }
        };
        await courseCollection.InsertManyAsync(courses);

        var lesson1 = new Lesson
        {
            Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            CourseId = courses[0].Id,
            TeacherId = teachers[0].Id,
            Grades = new List<Grade>(),
            Date = new DateTime(2026, 9, 1, 10, 0, 0, DateTimeKind.Utc)
        };

        var lesson2 = new Lesson
        {
            Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            CourseId = courses[0].Id,
            TeacherId = teachers[0].Id,
            Grades = new List<Grade>(),
            Date = new DateTime(2026, 9, 8, 10, 0, 0, DateTimeKind.Utc)
        };

        var lesson3 = new Lesson
        {
            Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(),
            CourseId = courses[1].Id,
            TeacherId = teachers[1].Id,
            Grades = new List<Grade>(),
            Date = new DateTime(2026, 9, 2, 14, 0, 0, DateTimeKind.Utc)
        };

        var lessons = new List<Lesson> { lesson1, lesson2, lesson3 };
        await lessonCollection.InsertManyAsync(lessons);

        courses[0].Lessons.Add(lesson1);
        courses[0].Lessons.Add(lesson2);
        courses[1].Lessons.Add(lesson3);

        var grades = new List<Grade>
        {
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), StudentId = students[0].Id, LessonId = lesson1.Id, Value = 95.0 },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), StudentId = students[1].Id, LessonId = lesson1.Id, Value = 88.0 },
            new() { Id = MongoDB.Bson.ObjectId.GenerateNewId().ToString(), StudentId = students[1].Id, LessonId = lesson3.Id, Value = 92.0 }
        };
        await gradeCollection.InsertManyAsync(grades);

        lesson1.Grades.Add(grades[0]);
        lesson1.Grades.Add(grades[1]);
        lesson3.Grades.Add(grades[2]);

        await lessonCollection.ReplaceOneAsync(l => l.Id == lesson1.Id, lesson1);
        await lessonCollection.ReplaceOneAsync(l => l.Id == lesson3.Id, lesson3);

        var tc1 = new TeacherCourse { TeacherId = teachers[0].Id, CourseId = courses[0].Id, TeacherName = teachers[0].Name, CourseName = courses[0].Name };
        var tc2 = new TeacherCourse { TeacherId = teachers[0].Id, CourseId = courses[2].Id, TeacherName = teachers[0].Name, CourseName = courses[2].Name };
        var tc3 = new TeacherCourse { TeacherId = teachers[1].Id, CourseId = courses[1].Id, TeacherName = teachers[1].Name, CourseName = courses[1].Name };

        teachers[0].TeacherCourses.Add(tc1);
        teachers[0].TeacherCourses.Add(tc2);
        teachers[1].TeacherCourses.Add(tc3);

        courses[0].TeacherCourses.Add(tc1);
        courses[2].TeacherCourses.Add(tc2);
        courses[1].TeacherCourses.Add(tc3);

        var sc1 = new StudentCourse { StudentId = students[0].Id, CourseId = courses[0].Id, StudentName = students[0].Name, CourseName = courses[0].Name };
        var sc2 = new StudentCourse { StudentId = students[1].Id, CourseId = courses[0].Id, StudentName = students[1].Name, CourseName = courses[0].Name };
        var sc3 = new StudentCourse { StudentId = students[1].Id, CourseId = courses[1].Id, StudentName = students[1].Name, CourseName = courses[1].Name };
        var sc4 = new StudentCourse { StudentId = students[2].Id, CourseId = courses[1].Id, StudentName = students[2].Name, CourseName = courses[1].Name };

        students[0].StudentCourses.Add(sc1);
        students[1].StudentCourses.Add(sc2);
        students[1].StudentCourses.Add(sc3);
        students[2].StudentCourses.Add(sc4);

        courses[0].StudentCourses.Add(sc1);
        courses[0].StudentCourses.Add(sc2);
        courses[1].StudentCourses.Add(sc3);
        courses[1].StudentCourses.Add(sc4);

        foreach (var course in courses)
        {
            await courseCollection.ReplaceOneAsync(c => c.Id == course.Id, course);
        }

        foreach (var teacher in teachers)
        {
            await teacherCollection.ReplaceOneAsync(t => t.Id == teacher.Id, teacher);
        }

        foreach (var student in students)
        {
            await studentCollection.ReplaceOneAsync(s => s.Id == student.Id, student);
        }
    }
}