using Microsoft.EntityFrameworkCore;
using Technizer.TaskApi.Data;

namespace Technizer.TaskApi.Users;

public interface IUserRepository
{
    Task<User?> FindByEmailAsync(string email);

    Task<bool> ExistsByEmailAsync(string email);

    Task<User> SaveAsync(User user);
}

public class UserRepository : IUserRepository
{
    private readonly AppDbContext _db;

    public UserRepository(AppDbContext db)
    {
        _db = db;
    }

    public Task<User?> FindByEmailAsync(string email) =>
        _db.Users.FirstOrDefaultAsync(u => u.Email == email);

    public Task<bool> ExistsByEmailAsync(string email) =>
        _db.Users.AnyAsync(u => u.Email == email);

    public async Task<User> SaveAsync(User user)
    {
        if (user.Id == 0)
        {
            _db.Users.Add(user);
        }
        await _db.SaveChangesAsync();
        return user;
    }
}
