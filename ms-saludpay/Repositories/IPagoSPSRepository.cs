using SPS.SaludPay.Models;

namespace SPS.SaludPay.Repositories;

public interface IPagoSPSRepository
{
    Task<PagoSPS?> FindByNumeroCompraAsync(long numeroCompra);
    Task<IEnumerable<PagoSPS>> FindByCedulaAndEstadoAsync(string cedula, string estado);
    Task<PagoSPS> SaveAsync(PagoSPS pago);
    Task<PagoSPS> UpdateAsync(PagoSPS pago);
}
