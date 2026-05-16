using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace LabTask4.Domain;

public class Student
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    public int Age { get; set; }
    public string Name { get; set; }
    public string Email { get; set; }
    public DateTime DateOfBirth { get; set; }
    public List<StudentCourse> StudentCourses { get; set; } = new();
}