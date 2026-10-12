using System.Security.Claims;
using System.Text.Encodings.Web;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;

namespace Technizer.TaskApi.Auth;

public class JwtAuthHandler : AuthenticationHandler<AuthenticationSchemeOptions>
{
    public const string SchemeName = "Bearer";

    private readonly JwtUtil _jwtUtil;

    public JwtAuthHandler(
        IOptionsMonitor<AuthenticationSchemeOptions> options,
        ILoggerFactory logger,
        UrlEncoder encoder,
        JwtUtil jwtUtil)
        : base(options, logger, encoder)
    {
        _jwtUtil = jwtUtil;
    }

    protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
    {
        string? header = Request.Headers.Authorization;

        if (header is null || !header.StartsWith("Bearer "))
        {
            return AuthenticateResult.NoResult();
        }

        string token = header["Bearer ".Length..];

        if (!_jwtUtil.IsTokenValid(token))
        {
            return AuthenticateResult.NoResult();
        }

        long userId;
        try
        {
            userId = await _jwtUtil.ExtractUserIdAsync(token);
        }
        catch (Exception ex)
        {
            return AuthenticateResult.Fail(ex);
        }

        var identity = new ClaimsIdentity(
            new[] { new Claim(ClaimTypes.NameIdentifier, userId.ToString()) },
            SchemeName);
        return AuthenticateResult.Success(new AuthenticationTicket(new ClaimsPrincipal(identity), SchemeName));
    }
}

public static class ClaimsPrincipalExtensions
{
    // Equivalent of Spring's @AuthenticationPrincipal Long ownerId.
    public static long GetUserId(this ClaimsPrincipal principal) =>
        long.Parse(principal.FindFirstValue(ClaimTypes.NameIdentifier)!);
}
