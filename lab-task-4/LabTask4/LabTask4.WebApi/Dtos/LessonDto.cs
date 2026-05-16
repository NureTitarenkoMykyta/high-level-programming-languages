using System.ComponentModel.DataAnnotations;

namespace LabTask4.Dtos;

public class LessonDto
{
    [Required(ErrorMessage = "Course reference ID is required.")]
    [RegularExpression(@"^[0-9a-fA-F]{24}$", ErrorMessage = "Invalid Course ID format. Must be a 24-digit hex string.")]
    public string CourseId { get; set; } = string.Empty;

    [Required(ErrorMessage = "Teacher reference ID is required.")]
    [RegularExpression(@"^[0-9a-fA-F]{24}$", ErrorMessage = "Invalid Teacher ID format. Must be a 24-digit hex string.")]
    public string TeacherId { get; set; } = string.Empty;

    [Required(ErrorMessage = "Lesson date and time is required.")]
    public DateTime? Date { get; set; }
}