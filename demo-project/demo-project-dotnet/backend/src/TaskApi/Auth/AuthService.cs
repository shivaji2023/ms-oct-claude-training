using Technizer.TaskApi.Users;

namespace Technizer.TaskApi.Auth;

public class AuthService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordEncoder _passwordEncoder;
    private readonly JwtUtil _jwtUtil;

    public AuthService(IUserRepository userRepository, IPasswordEncoder passwordEncoder, JwtUtil jwtUtil)
    {
        _userRepository = userRepository;
        _passwordEncoder = passwordEncoder;
        _jwtUtil = jwtUtil;
    }

    public async Task<string> RegisterAsync(string email, string password, string displayName)
    {
        if (await _userRepository.ExistsByEmailAsync(email))
        {
            throw new ArgumentException($"Email already registered: {email}");
        }
        var user = new User(email, _passwordEncoder.Encode(password), displayName);
        var saved = await _userRepository.SaveAsync(user);
        return _jwtUtil.GenerateToken(saved.Id, saved.Email);
    }

    public async Task<string> LoginAsync(string email, string password)
    {
        var user = await _userRepository.FindByEmailAsync(email)
                   ?? throw new ArgumentException("Invalid credentials");

        if (!_passwordEncoder.Matches(password, user.PasswordHash))
        {
            throw new ArgumentException("Invalid credentials");
        }
        return _jwtUtil.GenerateToken(user.Id, user.Email);
    }
}
