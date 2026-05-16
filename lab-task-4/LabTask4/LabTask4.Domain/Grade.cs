using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace LabTask4.Domain;

public class Grade
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }
    [BsonRepresentation(BsonType.ObjectId)]
    public string StudentId { get; set; }
    [BsonRepresentation(BsonType.ObjectId)]
    public string LessonId { get; set; }
    public double Value { get; set; }
}