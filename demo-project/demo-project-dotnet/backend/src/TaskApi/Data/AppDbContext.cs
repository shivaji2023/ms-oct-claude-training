using Microsoft.EntityFrameworkCore;
using Technizer.TaskApi.Tasks;
using Technizer.TaskApi.Users;

namespace Technizer.TaskApi.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();

    public DbSet<TaskItem> Tasks => Set<TaskItem>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(user =>
        {
            user.ToTable("app_user");
            user.HasIndex(u => u.Email).IsUnique();
        });

        modelBuilder.Entity<TaskItem>(task =>
        {
            task.ToTable("task");
        });
    }
}
