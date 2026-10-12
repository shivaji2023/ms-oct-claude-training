using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Technizer.TaskApi.Auth;

[ApiController]
[AllowAnonymous]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly AuthService _authService;

    public AuthController(AuthService authService)
    {
        _authService = authService;
    }

    [HttpPost("register")]
    public async Task<TokenResponse> Register([FromBody] RegisterRequest request)
    {
        string token = await _authService.RegisterAsync(request.Email, request.Password, request.DisplayName);
        return new TokenResponse(token);
    }

    [HttpPost("login")]
    public async Task<TokenResponse> Login([FromBody] LoginRequest request)
    {
        string token = await _authService.LoginAsync(request.Email, request.Password);
        return new TokenResponse(token);
    }

    public record RegisterRequest([EmailAddress] string Email, [Required] string Password, [Required] string DisplayName);

    public record LoginRequest([EmailAddress] string Email, [Required] string Password);

    public record TokenResponse(string Token);
}
