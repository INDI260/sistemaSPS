using SPS.SaludPay.DTOs;

namespace SPS.SaludPay.Services;

public interface ISaludPayService
{
    Task RegistrarCompraPendienteAsync(CompraPendienteDto dto);
    Task<IEnumerable<PagoSPSDto>> ObtenerCompraPendienteAsync(string cedula);
    Task<string> LoginAsync(LoginSaludPayDto dto);
    Task PagarAsync(PagoRequestDto dto, string cedula);
}
