using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using SPS.SaludPay.Frontend.DTOs;
using SPS.SaludPay.Frontend.Services;

namespace SPS.SaludPay.Frontend.Pages;

public class MisComprasModel : PageModel
{
    private readonly SaludPayApiClient _api;

    public List<PagoSPSDto> Compras { get; set; } = new();

    public MisComprasModel(SaludPayApiClient api)
    {
        _api = api;
    }

    public async Task<IActionResult> OnGetAsync()
    {
        var token = HttpContext.Session.GetString("token");
        var cedula = HttpContext.Session.GetString("cedula");

        if (string.IsNullOrEmpty(token) || string.IsNullOrEmpty(cedula))
            return RedirectToPage("/Login");

        Compras = await _api.ObtenerComprasAsync(cedula, token);
        return Page();
    }
}
