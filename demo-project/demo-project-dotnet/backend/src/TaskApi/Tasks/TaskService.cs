namespace Technizer.TaskApi.Tasks;

public class TaskService
{
    private readonly ITaskRepository _taskRepository;

    public TaskService(ITaskRepository taskRepository)
    {
        _taskRepository = taskRepository;
    }

    public Task<List<TaskItem>> ListForOwnerAsync(long ownerId) =>
        _taskRepository.FindByOwnerIdAsync(ownerId);

    public Task<TaskItem> CreateAsync(long ownerId, string title, string? description)
    {
        var task = new TaskItem(title, description, ownerId);
        return _taskRepository.SaveAsync(task);
    }

    public async Task<TaskItem> MarkCompletedAsync(long ownerId, long taskId)
    {
        var task = await _taskRepository.FindByIdAsync(taskId)
                   ?? throw new KeyNotFoundException($"Task not found: {taskId}");

        // NOTE: no ownership check here yet - flagged as a TODO for the
        // security-review demo (a user can complete another user's task).
        task.Completed = true;
        return await _taskRepository.SaveAsync(task);
    }

    public Task DeleteAsync(long ownerId, long taskId) =>
        _taskRepository.DeleteByIdAsync(taskId);
}
