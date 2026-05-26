using Microsoft.IdentityModel.Tokens;
using SPS.SaludPay.Models;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace SPS.SaludPay.Services;

public class JwtSaludPayService
{
    private readonly IConfiguration _config;

    public JwtSaludPayService(IConfiguration config)
    {
        _config = config;
    }

    public string GenerarToken(UsuarioSaludPay usuario)
    {
        var secret = _config["Jwt:Secret"]!;
        var expirySeconds = int.TryParse(_config["Jwt:ExpirySeconds"], out var s) ? s : 3600;

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secret));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            new Claim(JwtRegisteredClaimNames.Sub, usuario.Cedula),
            new Claim("cedula", usuario.Cedula),
            new Claim("nombre", usuario.Nombre)
        };

        var token = new JwtSecurityToken(
            claims: claims,
            expires: DateTime.UtcNow.AddSeconds(expirySeconds),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
