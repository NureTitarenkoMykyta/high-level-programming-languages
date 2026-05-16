using System.ComponentModel.DataAnnotations;

namespace LabTask4.Dtos;

public class GradeDto
{
    [Required(ErrorMessage = "Student reference ID is required.")]
    [RegularExpression(@"^[0-9a-fA-F]{24}$", ErrorMessage = "Invalid Student ID format. Must be a 24-digit hex string.")]
    public string StudentId { get; set; } = string.Empty;

    [Required(ErrorMessage = "Lesson reference ID is required.")]
    [RegularExpression(@"^[0-9a-fA-F]{24}$", ErrorMessage = "Invalid Lesson ID format. Must be a 24-digit hex string.")]
    public string LessonId { get; set; } = string.Empty;

    [Required(ErrorMessage = "Grade value is required.")]
    [Range(0.0, 100.0, ErrorMessage = "Grade score must rest between 0.0 and 100.0 points.")]
    public double Value { get; set; }
}