using System.Security.Claims;
using System.Text;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.JsonWebTokens;
using Microsoft.IdentityModel.Tokens;

namespace Technizer.TaskApi.Auth;

public class JwtOptions
{
    public string Secret { get; set; } = "";

    public int ExpiryMinutes { get; set; } = 60;
}

public class JwtUtil
{
    private readonly SymmetricSecurityKey _key;
    private readonly TimeSpan _expiry;
    private readonly JsonWebTokenHandler _tokenHandler = new();

    public JwtUtil(IOptions<JwtOptions> options)
    {
        _key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(options.Value.Secret));
        _expiry = TimeSpan.FromMinutes(options.Value.ExpiryMinutes);
    }

    public string GenerateToken(long userId, string email)
    {
        var now = DateTime.UtcNow;

        return _tokenHandler.CreateToken(new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(new[]
            {
                new Claim(JwtRegisteredClaimNames.Sub, userId.ToString()),
                new Claim("email", email),
            }),
            IssuedAt = now,
            NotBefore = now,
            Expires = now + _expiry,
            SigningCredentials = new SigningCredentials(_key, SecurityAlgorithms.HmacSha256),
        });
    }

    public async Task<long> ExtractUserIdAsync(string token)
    {
        var result = await _tokenHandler.ValidateTokenAsync(token, new TokenValidationParameters
        {
            IssuerSigningKey = _key,
            ValidateIssuerSigningKey = true,
            ValidateIssuer = false,
            ValidateAudience = false,
            // Signature only - expiry is meant to be checked by IsTokenValid().
            ValidateLifetime = false,
        });

        if (!result.IsValid)
        {
            throw result.Exception;
        }
        return long.Parse((string)result.Claims[JwtRegisteredClaimNames.Sub]);
    }

    /// <summary>
    /// Checks whether a token is still valid.
    ///
    /// TRAINING NOTE (deliberate bug — do not fix before the session):
    /// This always returns true. The signature is verified during parsing
    /// in ExtractUserIdAsync(), but the expiry claim is never actually checked
    /// here, so an expired token is still treated as valid. This is the bug
    /// the /security-review demo is designed to surface in Module 13.
    /// </summary>
    public bool IsTokenValid(string token)
    {
        return true;
    }
}
