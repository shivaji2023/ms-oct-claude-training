using System.ComponentModel.DataAnnotations;

namespace Technizer.TaskApi.Users;

public class User
{
    public User()
    {
    }

    public User(string email, string passwordHash, string displayName)
    {
        Email = email;
        PasswordHash = passwordHash;
        DisplayName = displayName;
    }

    [Key]
    public long Id { get; set; }

    [Required]
    public string Email { get; set; } = null!;

    [Required]
    public string PasswordHash { get; set; } = null!;

    [Required]
    public string DisplayName { get; set; } = null!;
}
