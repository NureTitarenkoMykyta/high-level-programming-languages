using System.ComponentModel.DataAnnotations;

namespace LabTask4.Dtos;

public class TeacherDto
{
    [Required(ErrorMessage = "Teacher name is required.")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "Teacher name must be between 2 and 100 characters.")]
    public string Name { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email address is required.")]
    [EmailAddress(ErrorMessage = "Invalid email address format.")]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Date of birth is required.")]
    public DateTime? DateOfBirth { get; set; }
    
    public List<string> CourseIds { get; set; } = new();
}