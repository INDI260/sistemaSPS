using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using saludpay_frontend.Models;

namespace saludpay_frontend.Pages;

public class ComprasModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public List<CompraPendiente> Compras { get; set; } = new();
    public string NombreUsuario { get; set; } = string.Empty;
    public string? MensajeExito { get; set; }
    public string? ErrorMensaje { get; set; }

    public ComprasModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public async Task<IActionResult> OnGet()
    {
        var token = HttpContext.Session.GetString("Token");
        if (string.IsNullOrEmpty(token))
            return RedirectToPage("/Index");

        NombreUsuario = HttpContext.Session.GetString("Nombre") ?? "";
        await CargarCompras(token);
        return Page();
    }

    public async Task<IActionResult> OnPostPagar(int numeroCompra, decimal valor)
    {
        var token = HttpContext.Session.GetString("Token");
        if (string.IsNullOrEmpty(token))
            return RedirectToPage("/Index");

        NombreUsuario = HttpContext.Session.GetString("Nombre") ?? "";

        try
        {
            var client = _httpClientFactory.CreateClient("SaludPayApi");
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var request = new PagoRequest { NumeroCompra = numeroCompra, ValorPagado = valor };
            var json = JsonSerializer.Serialize(request, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await client.PostAsync("/api/pago", content);

            if (response.IsSuccessStatusCode)
            {
                MensajeExito = $"Pago de la compra N° {numeroCompra} realizado exitosamente.";
            }
            else
            {
                ErrorMensaje = "Error al procesar el pago. Intente nuevamente.";
            }
        }
        catch (Exception)
        {
            ErrorMensaje = "Error de conexión con el servidor de pagos.";
        }

        await CargarCompras(token);
        return Page();
    }

    private async Task CargarCompras(string token)
    {
        try
        {
            var client = _httpClientFactory.CreateClient("SaludPayApi");
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var response = await client.GetAsync("/api/compras/pendientes");
            if (response.IsSuccessStatusCode)
            {
                var body = await response.Content.ReadAsStringAsync();
                var compras = JsonSerializer.Deserialize<List<CompraPendiente>>(body, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
                if (compras != null)
                    Compras = compras;
            }
        }
        catch { }
    }
}
