using LabTask4.Infrastructure;
using LanguageDuel.Infrastructure;
using MongoDB.Driver;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddInfrastructureServices(builder.Configuration);
builder.Services.AddAutoMapper(_ => { }, AppDomain.CurrentDomain.GetAssemblies());
builder.Services.AddOutputCache();

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var applicationDbContext = scope.ServiceProvider.GetRequiredService<IMongoDatabase>();
    await DbInitializer.CreateInitialDataAsync(applicationDbContext);
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseOutputCache();

app.MapControllers();

app.Run();