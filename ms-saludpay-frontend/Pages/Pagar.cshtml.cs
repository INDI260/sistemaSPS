using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using SPS.SaludPay.Frontend.Services;

namespace SPS.SaludPay.Frontend.Pages;

public class PagarModel : PageModel
{
    private readonly SaludPayApiClient _api;

    public long NumeroCompra { get; set; }
    public decimal Valor { get; set; }
    public bool PagoExitoso { get; set; }
    public string? ErrorMessage { get; set; }

    public PagarModel(SaludPayApiClient api)
    {
        _api = api;
    }

    public IActionResult OnGet(long numeroCompra, decimal valor)
    {
        var token = HttpContext.Session.GetString("token");
        if (string.IsNullOrEmpty(token))
            return RedirectToPage("/Login");

        NumeroCompra = numeroCompra;
        Valor = valor;
        return Page();
    }

    public async Task<IActionResult> OnPostAsync(long numeroCompra, decimal valor)
    {
        var token = HttpContext.Session.GetString("token");
        if (string.IsNullOrEmpty(token))
            return RedirectToPage("/Login");

        NumeroCompra = numeroCompra;
        Valor = valor;

        var (ok, error) = await _api.PagarAsync(numeroCompra, valor, token);

        if (ok)
        {
            PagoExitoso = true;
        }
        else
        {
            ErrorMessage = error ?? "No se pudo procesar el pago.";
        }

        return Page();
    }
}
