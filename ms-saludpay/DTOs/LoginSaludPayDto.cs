namespace SPS.SaludPay.DTOs;

public record LoginSaludPayDto(string Cedula, string Contrasena);

public record LoginResponseDto(string Token);
