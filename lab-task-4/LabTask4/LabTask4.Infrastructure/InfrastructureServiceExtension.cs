using LabTask4.Application.Repositories;
using LabTask4.Infrastructure.Repositories;
using LanguageDuel.Infrastructure.Repositories;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using MongoDB.Driver;

namespace LanguageDuel.Infrastructure;

public static class InfrastructureServiceExtension
{
    public static IServiceCollection AddInfrastructureServices(this IServiceCollection services, ConfigurationManager configuration)
    {
        var connectionString = configuration.GetConnectionString("DefaultConnection") ?? throw new InvalidOperationException("Connection string 'DefaultConnection' not found.");
        services.AddSingleton<IMongoClient>(new MongoClient(connectionString));
        
        services.AddScoped<IUnitOfWork, UnitOfWork>();
        
        services.AddScoped(typeof(IRepository<>), typeof(Repository<>));

        services.AddScoped<LessonRepository>();
        services.AddScoped<TeacherRepository>();
        services.AddScoped<GradeRepository>();
        services.AddScoped<StudentRepository>();
        services.AddScoped<CourseRepository>();

        services.AddScoped(sp =>
        {
            var client = sp.GetRequiredService<IMongoClient>();
            var mongoUrl = new MongoUrl(connectionString);
            return client.GetDatabase(mongoUrl.DatabaseName);
        });
        
        return services;
    }
}