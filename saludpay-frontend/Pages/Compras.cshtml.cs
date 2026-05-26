using System.Net.Http.Headers;
using System.Text.Json.Serialization;
using System.Text;
using System.Text.Json;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using Microsoft.AspNetCore.Antiforgery;
using saludpay_frontend.Models;

namespace saludpay_frontend.Pages;

public class CompraWrapper
{
    [JsonPropertyName("status")]
    public string Status { get; set; } = "";
    [JsonPropertyName("data")]
    public List<CompraItem> Data { get; set; } = new();
    [JsonPropertyName("message")]
    public string Message { get; set; } = "";
}

public class CompraItem
{
    [JsonPropertyName("numeroCompra")]
    public int NumeroCompra { get; set; }
    [JsonPropertyName("cedulaCliente")]
    public string CedulaCliente { get; set; } = "";
    [JsonPropertyName("nombreCliente")]
    public string NombreCliente { get; set; } = "";
    [JsonPropertyName("precioTotal")]
    public decimal PrecioTotal { get; set; }
    [JsonPropertyName("estado")]
    public string Estado { get; set; } = "";
}

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

    [IgnoreAntiforgeryToken]
    public async Task<IActionResult> OnPostPagar(int numeroCompra, decimal valor)
    {
        var token = HttpContext.Session.GetString("Token");
        if (string.IsNullOrEmpty(token))
            return RedirectToPage("/Index");

        NombreUsuario = HttpContext.Session.GetString("Nombre") ?? "";

        try
        {
            var client = _httpClientFactory.CreateClient("CompraApi");

            var request = new PagoRequest { NumeroCompra = numeroCompra, ValorPagado = valor };
            var json = JsonSerializer.Serialize(request, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
            var content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await client.PostAsync($"{numeroCompra}/pago", content);

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
            var client = _httpClientFactory.CreateClient("CompraApi");
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
            var cedula = HttpContext.Session.GetString("Cedula") ?? "";
            var response = await client.GetAsync($"cliente/{cedula}");
            if (response.IsSuccessStatusCode)
            {
                var body = await response.Content.ReadAsStringAsync();
                var wrapper = JsonSerializer.Deserialize<CompraWrapper>(body, new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase });
                if (wrapper?.Data != null)
                    Compras = wrapper.Data
                        .Where(c => c.Estado == "PENDIENTE_PAGO")
                        .Select(c => new CompraPendiente
                        {
                            NumeroCompra = c.NumeroCompra,
                            CedulaCliente = c.CedulaCliente,
                            NombreCliente = c.NombreCliente,
                            Valor = c.PrecioTotal,
                            Estado = c.Estado
                        }).ToList();
            }
        }
        catch { }
    }
}
