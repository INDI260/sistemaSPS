using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SPS.SaludPay.Data;
using SPS.SaludPay.Models;
using SPS.SaludPay.Repositories;
using SPS.SaludPay.Services;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Database — versión hardcodeada para evitar conexión en tiempo de registro
var connectionString = builder.Configuration.GetConnectionString("SaludPayDb");
builder.Services.AddDbContext<SaludPayDbContext>(options =>
    options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 36))));

// Repositories
builder.Services.AddScoped<IPagoSPSRepository, PagoSPSRepository>();
builder.Services.AddScoped<IUsuarioSaludPayRepository, UsuarioSaludPayRepository>();

// Services
builder.Services.AddScoped<ISaludPayService, SaludPayService>();
builder.Services.AddScoped<JwtSaludPayService>();

// HTTP client para callback a ms-compra
builder.Services.AddHttpClient();

// JWT Authentication
var jwtSecret = builder.Configuration["Jwt:Secret"]!;
var keyBytes = Encoding.UTF8.GetBytes(jwtSecret);

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(keyBytes),
            ValidateIssuer = false,
            ValidateAudience = false,
            ClockSkew = TimeSpan.Zero
        };
    });

builder.Services.AddAuthorization();

// JSON camelCase
builder.Services.AddControllers()
    .AddJsonOptions(opts =>
        opts.JsonSerializerOptions.PropertyNamingPolicy =
            System.Text.Json.JsonNamingPolicy.CamelCase);

var app = builder.Build();

// Crear esquema y seed — tolerante a fallo de DB
using (var scope = app.Services.CreateScope())
{
    try
    {
        var db = scope.ServiceProvider.GetRequiredService<SaludPayDbContext>();
        var logger = scope.ServiceProvider.GetRequiredService<ILogger<Program>>();

        db.Database.EnsureCreated();

        if (!db.UsuariosSaludPay.Any())
        {
            db.UsuariosSaludPay.Add(new UsuarioSaludPay
            {
                Cedula = "1000000001",
                Nombre = "Carlos Pérez",
                Contrasena = BCrypt.Net.BCrypt.HashPassword("Password123!", 12),
                Correo = "carlos@test.com",
                Activo = true
            });
            db.SaveChanges();
            logger.LogInformation("Usuario de prueba creado.");
        }
    }
    catch (Exception ex)
    {
        var logger = scope.ServiceProvider.GetRequiredService<ILogger<Program>>();
        logger.LogError(ex, "No se pudo conectar a la base de datos al iniciar. Verifique MySQL.");
    }
}

app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
