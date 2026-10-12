using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Mvc;
using Technizer.TaskApi.Auth;

namespace Technizer.TaskApi.Tasks;

[ApiController]
[Route("api/tasks")]
public class TaskController : ControllerBase
{
    private readonly TaskService _taskService;

    public TaskController(TaskService taskService)
    {
        _taskService = taskService;
    }

    [HttpGet]
    public Task<List<TaskItem>> List() =>
        _taskService.ListForOwnerAsync(User.GetUserId());

    [HttpPost]
    public Task<TaskItem> Create([FromBody] CreateTaskRequest request) =>
        _taskService.CreateAsync(User.GetUserId(), request.Title, request.Description);

    [HttpPatch("{id:long}/complete")]
    public Task<TaskItem> Complete(long id) =>
        _taskService.MarkCompletedAsync(User.GetUserId(), id);

    [HttpDelete("{id:long}")]
    public async Task<IActionResult> Delete(long id)
    {
        await _taskService.DeleteAsync(User.GetUserId(), id);
        return Ok();
    }

    public record CreateTaskRequest([Required(AllowEmptyStrings = false)] string Title, string? Description);
}
