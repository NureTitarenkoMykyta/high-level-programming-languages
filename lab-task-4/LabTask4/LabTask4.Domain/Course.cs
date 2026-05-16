using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace LabTask4.Domain;

public class Course
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    public string Name { get; set; }
    public string Code { get; set; }
    public List<StudentCourse> StudentCourses { get; set; } = new();
    public List<TeacherCourse> TeacherCourses { get; set; } = new();
    public List<Lesson> Lessons { get; set; }
}