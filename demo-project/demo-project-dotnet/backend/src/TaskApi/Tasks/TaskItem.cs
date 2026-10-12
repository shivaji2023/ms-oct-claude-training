using System.ComponentModel.DataAnnotations;

namespace Technizer.TaskApi.Tasks;

// Named TaskItem rather than Task to avoid clashing with System.Threading.Tasks.Task.
public class TaskItem
{
    public TaskItem()
    {
    }

    public TaskItem(string title, string? description, long ownerId)
    {
        Title = title;
        Description = description;
        OwnerId = ownerId;
    }

    [Key]
    public long Id { get; set; }

    [Required]
    public string Title { get; set; } = null!;

    public string? Description { get; set; }

    public bool Completed { get; set; }

    public long OwnerId { get; set; }

    public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
}
