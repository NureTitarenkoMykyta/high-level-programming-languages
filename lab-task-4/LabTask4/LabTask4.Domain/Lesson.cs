using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace LabTask4.Domain;

public class Lesson
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    [BsonRepresentation(BsonType.ObjectId)]
    public string CourseId { get; set; }
    [BsonRepresentation(BsonType.ObjectId)]
    public string TeacherId { get; set; }
    public List<Grade> Grades { get; set; }
    public DateTime Date { get; set; }
}