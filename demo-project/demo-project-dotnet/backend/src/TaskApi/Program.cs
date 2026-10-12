using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Technizer.TaskApi.Auth;
using Technizer.TaskApi.Config;
using Technizer.TaskApi.Data;
using Technizer.TaskApi.Exceptions;
using Technizer.TaskApi.Tasks;
using Technizer.TaskApi.Users;

var builder = WebApplication.CreateBuilder(args);

// An in-memory SQLite database lives only as long as at least one connection
// to it is open, so keep one open for the lifetime of the app.
var keepAliveConnection = new SqliteConnection(builder.Configuration.GetConnectionString("TaskDb"));
keepAliveConnection.Open();
builder.Services.AddSingleton(keepAliveConnection);
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite(builder.Configuration.GetConnectionString("TaskDb")));

builder.Services.AddScoped<IUserRepository, UserRepository>();
builder.Services.AddScoped<ITaskRepository, TaskRepository>();
builder.Services.AddScoped<AuthService>();
builder.Services.AddScoped<TaskService>();

builder.Services.AddSecurity(builder.Configuration);
builder.Services.AddExceptionHandling();
builder.Services.AddControllers();

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    scope.ServiceProvider.GetRequiredService<AppDbContext>().Database.EnsureCreated();
}

app.UseExceptionHandler();
app.UseCors(SecurityConfig.CorsPolicy);
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();

// Exposed so integration tests can use WebApplicationFactory<Program>.
public partial class Program;
