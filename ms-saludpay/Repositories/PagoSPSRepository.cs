using Microsoft.EntityFrameworkCore;
using SPS.SaludPay.Data;
using SPS.SaludPay.Models;

namespace SPS.SaludPay.Repositories;

public class PagoSPSRepository : IPagoSPSRepository
{
    private readonly SaludPayDbContext _context;

    public PagoSPSRepository(SaludPayDbContext context)
    {
        _context = context;
    }

    public async Task<PagoSPS?> FindByNumeroCompraAsync(long numeroCompra)
        => await _context.PagosSPS.FirstOrDefaultAsync(p => p.NumeroCompra == numeroCompra);

    public async Task<IEnumerable<PagoSPS>> FindByCedulaAndEstadoAsync(string cedula, string estado)
        => await _context.PagosSPS
            .Where(p => p.CedulaCliente == cedula && p.Estado == estado)
            .ToListAsync();

    public async Task<PagoSPS> SaveAsync(PagoSPS pago)
    {
        _context.PagosSPS.Add(pago);
        await _context.SaveChangesAsync();
        return pago;
    }

    public async Task<PagoSPS> UpdateAsync(PagoSPS pago)
    {
        _context.PagosSPS.Update(pago);
        await _context.SaveChangesAsync();
        return pago;
    }
}
