using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SPS.SaludPay.DTOs;
using SPS.SaludPay.Services;
using System.Security.Claims;

namespace SPS.SaludPay.Controllers;

[ApiController]
[Route("ws/saludpay")]
public class WSSaludPayController : ControllerBase
{
    private readonly ISaludPayService _service;
    private readonly IConfiguration _config;

    public WSSaludPayController(ISaludPayService service, IConfiguration config)
    {
        _service = service;
        _config = config;
    }

    [HttpPost("auth/login")]
    public async Task<IActionResult> Login([FromBody] LoginSaludPayDto dto)
    {
        try
        {
            var token = await _service.LoginAsync(dto);
            var response = ApiResponseDto<LoginResponseDto>.Ok(new LoginResponseDto(token), "Login exitoso");
            return Ok(response);
        }
        catch (UnauthorizedAccessException ex)
        {
            return Unauthorized(ApiResponseDto<object?>.Error(ex.Message));
        }
    }

    [HttpPost("compras-pendientes")]
    public async Task<IActionResult> RecibirCompraPendiente([FromBody] CompraPendienteDto dto)
    {
        var internalKey = _config["SPS:InternalApiKey"];
        if (!Request.Headers.TryGetValue("X-Internal-Key", out var key) || key != internalKey)
            return Unauthorized(ApiResponseDto<object?>.Error("Clave interna inválida"));

        await _service.RegistrarCompraPendienteAsync(dto);
        return Ok(ApiResponseDto<object?>.Ok(null, "Compra pendiente registrada"));
    }

    [Authorize]
    [HttpGet("compras/{cedula}")]
    public async Task<IActionResult> ObtenerCompras(string cedula)
    {
        var tokenCedula = User.FindFirst("cedula")?.Value;
        if (tokenCedula != cedula)
            return Forbid();

        var compras = await _service.ObtenerCompraPendienteAsync(cedula);
        return Ok(ApiResponseDto<IEnumerable<PagoSPSDto>>.Ok(compras, "Compras obtenidas"));
    }

    [Authorize]
    [HttpPost("pagar")]
    public async Task<IActionResult> Pagar([FromBody] PagoRequestDto dto)
    {
        var cedula = User.FindFirst("cedula")?.Value;
        if (string.IsNullOrEmpty(cedula))
            return Unauthorized(ApiResponseDto<object?>.Error("Token inválido"));

        try
        {
            await _service.PagarAsync(dto, cedula);
            return Ok(ApiResponseDto<object?>.Ok(null, "Pago procesado exitosamente"));
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ApiResponseDto<object?>.Error(ex.Message));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(ApiResponseDto<object?>.Error(ex.Message));
        }
    }
}
