using SPS.SaludPay.Models;

namespace SPS.SaludPay.Repositories;

public interface IUsuarioSaludPayRepository
{
    Task<UsuarioSaludPay?> FindByCedulaAsync(string cedula);
}
