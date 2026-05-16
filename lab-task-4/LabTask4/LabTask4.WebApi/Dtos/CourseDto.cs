using System.ComponentModel.DataAnnotations;

namespace LabTask4.Dtos;

public class CourseDto
{
    [Required(ErrorMessage = "Course name is required.")]
    [StringLength(100, MinimumLength = 3, ErrorMessage = "Course name must be between 3 and 100 characters.")]
    public string Name { get; set; } = string.Empty;

    [Required(ErrorMessage = "Course code is required.")]
    [StringLength(10, MinimumLength = 3, ErrorMessage = "Course code must be between 3 and 10 characters.")]
    [RegularExpression(@"^[A-Z]{2,4}-\d{3,4}$", ErrorMessage = "Course code must match format (e.g., CS-101 or MATH-2022).")]
    public string Code { get; set; } = string.Empty;
    
    public List<string> StudentIds { get; set; } = new();
    public List<string> TeacherIds { get; set; } = new();
}