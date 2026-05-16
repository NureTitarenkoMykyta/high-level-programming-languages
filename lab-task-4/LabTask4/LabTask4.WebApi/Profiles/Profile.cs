using AutoMapper;
using LabTask4.Domain;
using LabTask4.Dtos;

namespace LabTask4.Profiles;

public class Profile : AutoMapper.Profile
{
    public Profile()
    {
        CreateMap<StudentDto, Student>();
        CreateMap<LessonDto, Lesson>();
        CreateMap<CourseDto, Course>();
        CreateMap<TeacherDto, Teacher>();
        CreateMap<GradeDto, Grade>();
    }
}