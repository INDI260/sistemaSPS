using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using SPS.SaludPay.Frontend.Services;

namespace SPS.SaludPay.Frontend.Pages;

public class LoginModel : PageModel
{
    private readonly SaludPayApiClient _api;

    public string? ErrorMessage { get; set; }

    public LoginModel(SaludPayApiClient api)
    {
        _api = api;
    }

    public IActionResult OnGet()
    {
        if (HttpContext.Session.GetString("token") != null)
            return RedirectToPage("/MisCompras");
        return Page();
    }

    public async Task<IActionResult> OnPostAsync(string cedula, string contrasena)
    {
        if (string.IsNullOrWhiteSpace(cedula) || string.IsNullOrWhiteSpace(contrasena))
        {
            ErrorMessage = "Ingrese cédula y contraseña.";
            return Page();
        }

        var (ok, token, error) = await _api.LoginAsync(cedula, contrasena);

        if (!ok || token is null)
        {
            ErrorMessage = error ?? "Credenciales inválidas.";
            return Page();
        }

        HttpContext.Session.SetString("token", token);
        HttpContext.Session.SetString("cedula", cedula);

        return RedirectToPage("/MisCompras");
    }

    public IActionResult OnGetLogout()
    {
        HttpContext.Session.Clear();
        return RedirectToPage("/Login");
    }
}
