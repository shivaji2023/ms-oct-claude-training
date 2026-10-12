using Microsoft.EntityFrameworkCore;
using Technizer.TaskApi.Data;

namespace Technizer.TaskApi.Tasks;

public interface ITaskRepository
{
    Task<List<TaskItem>> FindByOwnerIdAsync(long ownerId);

    Task<TaskItem?> FindByIdAsync(long id);

    Task<TaskItem> SaveAsync(TaskItem task);

    Task DeleteByIdAsync(long id);
}

public class TaskRepository : ITaskRepository
{
    private readonly AppDbContext _db;

    public TaskRepository(AppDbContext db)
    {
        _db = db;
    }

    public Task<List<TaskItem>> FindByOwnerIdAsync(long ownerId) =>
        _db.Tasks.Where(t => t.OwnerId == ownerId).ToListAsync();

    public async Task<TaskItem?> FindByIdAsync(long id) =>
        await _db.Tasks.FindAsync(id);

    public async Task<TaskItem> SaveAsync(TaskItem task)
    {
        if (task.Id == 0)
        {
            _db.Tasks.Add(task);
        }
        await _db.SaveChangesAsync();
        return task;
    }

    public async Task DeleteByIdAsync(long id)
    {
        // Like Spring Data's deleteById, deleting a missing id is a no-op.
        await _db.Tasks.Where(t => t.Id == id).ExecuteDeleteAsync();
    }
}
