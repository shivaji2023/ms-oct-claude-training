namespace Technizer.TaskApi.Auth;

public interface IPasswordEncoder
{
    string Encode(string rawPassword);

    bool Matches(string rawPassword, string encodedPassword);
}

// Produces the same $2a$ BCrypt hashes as Spring Security's BCryptPasswordEncoder.
public class BCryptPasswordEncoder : IPasswordEncoder
{
    public string Encode(string rawPassword) => BCrypt.Net.BCrypt.HashPassword(rawPassword);

    public bool Matches(string rawPassword, string encodedPassword) =>
        BCrypt.Net.BCrypt.Verify(rawPassword, encodedPassword);
}
