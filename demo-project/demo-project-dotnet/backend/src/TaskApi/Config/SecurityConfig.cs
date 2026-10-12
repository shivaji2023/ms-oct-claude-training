using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authorization;
using Technizer.TaskApi.Auth;

namespace Technizer.TaskApi.Config;

public static class SecurityConfig
{
    public const string CorsPolicy = "frontend";

    public static IServiceCollection AddSecurity(this IServiceCollection services, IConfiguration configuration)
    {
        services.Configure<JwtOptions>(configuration.GetSection("App:Jwt"));
        services.AddSingleton<JwtUtil>();
        services.AddSingleton<IPasswordEncoder, BCryptPasswordEncoder>();

        services.AddAuthentication(JwtAuthHandler.SchemeName)
            .AddScheme<AuthenticationSchemeOptions, JwtAuthHandler>(JwtAuthHandler.SchemeName, null);

        // Every endpoint requires an authenticated user unless marked [AllowAnonymous]
        // (the /api/auth endpoints are).
        services.AddAuthorization(options =>
        {
            options.FallbackPolicy = new AuthorizationPolicyBuilder()
                .RequireAuthenticatedUser()
                .Build();
        });

        services.AddCors(options =>
        {
            options.AddPolicy(CorsPolicy, policy => policy
                .WithOrigins("http://localhost:4200")
                .WithMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .AllowAnyHeader());
        });

        return services;
    }
}
