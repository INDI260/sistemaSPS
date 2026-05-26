using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Antiforgery;
using saludpay_frontend.Models;

namespace saludpay_frontend.Pages;

public class AuthResponse
{
    [JsonPropertyName("status")]
    public string Status { get; set; } = "";
    [JsonPropertyName("data")]
    public AuthData? Data { get; set; }
}

public class AuthData
{
    [JsonPropertyName("token")]
    public string Token { get; set; } = "";
    [JsonPropertyName("cedula")]
    public string Cedula { get; set; } = "";
    [JsonPropertyName("nombre")]
    public string Nombre { get; set; } = "";
    [JsonPropertyName("rol")]
    public string Rol { get; set; } = "";
}

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

    [IgnoreAntiforgeryToken]
    public async Task<IActionResult> OnPost(string cedula, string contrasena)
    {
        try
        {
            var client = _httpClientFactory.CreateClient("NginxApi");
            var request = new LoginRequest { Cedula = cedula, Contrasena = contrasena };
            var json = JsonSerializer.Serialize(request, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await client.PostAsync("api/auth/login", content);

            if (response.IsSuccessStatusCode)
            {
                var responseBody = await response.Content.ReadAsStringAsync();
                var authResponse = JsonSerializer.Deserialize<AuthResponse>(responseBody, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });

                if (authResponse?.Data != null)
                {
                    HttpContext.Session.SetString("Token", authResponse.Data.Token);
                    HttpContext.Session.SetString("Nombre", authResponse.Data.Nombre);
                    HttpContext.Session.SetString("Cedula", authResponse.Data.Cedula);
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

    [IgnoreAntiforgeryToken]
    public IActionResult OnPostLogout()
    {
        HttpContext.Session.Clear();
        return RedirectToPage("/Index");
    }
}
