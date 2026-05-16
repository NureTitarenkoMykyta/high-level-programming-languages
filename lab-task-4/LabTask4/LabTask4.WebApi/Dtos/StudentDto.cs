using System.ComponentModel.DataAnnotations;

namespace LabTask4.Dtos;

public class StudentDto
{
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } = string.Empty;
    
    [Range(17, 120)]
    public int Age { get; set; }
    [EmailAddress]
    public string Email { get; set; } = string.Empty;
    [Required]
    public DateTime? DateOfBirth { get; set; }
    
    public List<string> CourseIds { get; set; } = new();
}