using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace LabTask4.Domain;

public class Teacher
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    public string Name { get; set; }
    public string Email { get; set; }
    public DateTime DateOfBirth { get; set; }
    public List<TeacherCourse> TeacherCourses { get; set; }
}