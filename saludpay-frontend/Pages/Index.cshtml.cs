using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using saludpay_frontend.Models;

namespace saludpay_frontend.Pages;

public class IndexModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public string? ErrorMensaje { get; set; }

    public IndexModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public void OnGet()
    {
        if (HttpContext.Session.GetString("Token") != null)
        {
            Response.Redirect("/Compras");
        }
    }

    public async Task<IActionResult> OnPost(string cedula, string contrasena)
    {
        try
        {
            var client = _httpClientFactory.CreateClient("SaludPayApi");
            var request = new LoginRequest { Cedula = cedula, Contrasena = contrasena };
            var json = JsonSerializer.Serialize(request, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await client.PostAsync("/api/auth/login", content);

            if (response.IsSuccessStatusCode)
            {
                var responseBody = await response.Content.ReadAsStringAsync();
                var loginResponse = JsonSerializer.Deserialize<LoginResponse>(responseBody, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });

                if (loginResponse != null)
                {
                    HttpContext.Session.SetString("Token", loginResponse.Token);
                    HttpContext.Session.SetString("Nombre", loginResponse.Nombre);
                    HttpContext.Session.SetString("Cedula", loginResponse.Cedula);
                    return RedirectToPage("/Compras");
                }
            }

            ErrorMensaje = "Cédula o contraseña incorrectos.";
            return Page();
        }
        catch (Exception)
        {
            ErrorMensaje = "Error de conexión con el servidor. Intente nuevamente.";
            return Page();
        }
    }

    public IActionResult OnPostLogout()
    {
        HttpContext.Session.Clear();
        return RedirectToPage("/Index");
    }
}
