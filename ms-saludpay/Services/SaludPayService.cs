using SPS.SaludPay.DTOs;
using SPS.SaludPay.Models;
using SPS.SaludPay.Repositories;
using System.Text;
using System.Text.Json;

namespace SPS.SaludPay.Services;

public class SaludPayService : ISaludPayService
{
    private readonly IPagoSPSRepository _pagoRepo;
    private readonly IUsuarioSaludPayRepository _usuarioRepo;
    private readonly JwtSaludPayService _jwtService;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IConfiguration _config;
    private readonly ILogger<SaludPayService> _logger;

    public SaludPayService(
        IPagoSPSRepository pagoRepo,
        IUsuarioSaludPayRepository usuarioRepo,
        JwtSaludPayService jwtService,
        IHttpClientFactory httpClientFactory,
        IConfiguration config,
        ILogger<SaludPayService> logger)
    {
        _pagoRepo = pagoRepo;
        _usuarioRepo = usuarioRepo;
        _jwtService = jwtService;
        _httpClientFactory = httpClientFactory;
        _config = config;
        _logger = logger;
    }

    public async Task RegistrarCompraPendienteAsync(CompraPendienteDto dto)
    {
        var existente = await _pagoRepo.FindByNumeroCompraAsync(dto.NumeroCompra);
        if (existente is not null)
        {
            _logger.LogWarning("Compra {NumeroCompra} ya registrada en SaludPay", dto.NumeroCompra);
            return;
        }

        var pago = new PagoSPS
        {
            CedulaCliente = dto.CedulaCliente,
            NumeroCompra = dto.NumeroCompra,
            ValorPendiente = dto.ValorPendiente,
            Estado = "PENDIENTE",
            FechaCreacion = DateTime.UtcNow
        };

        await _pagoRepo.SaveAsync(pago);
    }

    public async Task<IEnumerable<PagoSPSDto>> ObtenerCompraPendienteAsync(string cedula)
    {
        var pagos = await _pagoRepo.FindByCedulaAndEstadoAsync(cedula, "PENDIENTE");
        return pagos.Select(p => new PagoSPSDto(
            p.Id, p.NumeroCompra, p.ValorPendiente, p.Estado, p.FechaCreacion));
    }

    public async Task<string> LoginAsync(LoginSaludPayDto dto)
    {
        var usuario = await _usuarioRepo.FindByCedulaAsync(dto.Cedula)
            ?? throw new UnauthorizedAccessException("Credenciales inválidas");

        if (!usuario.Activo)
            throw new UnauthorizedAccessException("Usuario inactivo");

        if (!BCrypt.Net.BCrypt.Verify(dto.Contrasena, usuario.Contrasena))
            throw new UnauthorizedAccessException("Credenciales inválidas");

        return _jwtService.GenerarToken(usuario);
    }

    public async Task PagarAsync(PagoRequestDto dto, string cedula)
    {
        var pago = await _pagoRepo.FindByNumeroCompraAsync(dto.NumeroCompra)
            ?? throw new KeyNotFoundException($"Compra {dto.NumeroCompra} no encontrada");

        if (dto.ValorPagado < pago.ValorPendiente)
            throw new InvalidOperationException(
                $"El valor pagado ({dto.ValorPagado}) es menor al pendiente ({pago.ValorPendiente})");

        pago.Estado = "PAGADO";
        pago.ValorPagado = dto.ValorPagado;
        pago.FechaPago = DateTime.UtcNow;

        await _pagoRepo.UpdateAsync(pago);

        await NotificarCallbackCompraAsync(cedula, pago.NumeroCompra, dto.ValorPagado);
    }

    private async Task NotificarCallbackCompraAsync(string cedula, long numeroCompra, decimal valorPagado)
    {
        var callbackUrl = _config["SPS:CompraCallbackUrl"];
        var internalKey = _config["SPS:InternalApiKey"];

        var payload = new
        {
            cedulaCliente = cedula,
            numeroCompra,
            valorPagado
        };

        try
        {
            var client = _httpClientFactory.CreateClient();
            client.DefaultRequestHeaders.Add("X-Internal-Key", internalKey);

            var content = new StringContent(
                JsonSerializer.Serialize(payload),
                Encoding.UTF8,
                "application/json");

            var response = await client.PostAsync(
                $"{callbackUrl}/ws/compra/{numeroCompra}/pago", content);

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogError(
                    "Callback a ms-compra falló. Compra={NumeroCompra} Status={Status}",
                    numeroCompra, response.StatusCode);
            }
        }
        catch (Exception ex)
        {
            _logger.LogError(ex,
                "Error al notificar pago a ms-compra. Compra={NumeroCompra}", numeroCompra);
        }
    }
}
