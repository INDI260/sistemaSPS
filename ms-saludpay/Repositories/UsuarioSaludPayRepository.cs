using Microsoft.EntityFrameworkCore;
using SPS.SaludPay.Data;
using SPS.SaludPay.Models;

namespace SPS.SaludPay.Repositories;

public class UsuarioSaludPayRepository : IUsuarioSaludPayRepository
{
    private readonly SaludPayDbContext _context;

    public UsuarioSaludPayRepository(SaludPayDbContext context)
    {
        _context = context;
    }

    public async Task<UsuarioSaludPay?> FindByCedulaAsync(string cedula)
        => await _context.UsuariosSaludPay.FirstOrDefaultAsync(u => u.Cedula == cedula);
}
