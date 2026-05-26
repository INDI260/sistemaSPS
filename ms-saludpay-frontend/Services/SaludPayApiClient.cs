using SPS.SaludPay.Frontend.DTOs;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace SPS.SaludPay.Frontend.Services;

public class SaludPayApiClient
{
    private readonly HttpClient _httpClient;
    private readonly string _baseUrl;

    private static readonly JsonSerializerOptions _jsonOpts = new()
    {
        PropertyNameCaseInsensitive = true,
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    public SaludPayApiClient(HttpClient httpClient, IConfiguration config)
    {
        _httpClient = httpClient;
        _baseUrl = config["SaludPayApi:BaseUrl"]!;
    }

    public async Task<(bool ok, string? token, string? error)> LoginAsync(string cedula, string contrasena)
    {
        try
        {
            var body = JsonSerializer.Serialize(new { cedula, contrasena }, _jsonOpts);
            var content = new StringContent(body, Encoding.UTF8, "application/json");

            var response = await _httpClient.PostAsync($"{_baseUrl}/ws/saludpay/auth/login", content);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                var err = JsonSerializer.Deserialize<ApiResponseWrapper<object>>(json, _jsonOpts);
                return (false, null, err?.Message ?? "Error al iniciar sesión");
            }

            var result = JsonSerializer.Deserialize<ApiResponseWrapper<LoginData>>(json, _jsonOpts);
            return (true, result?.Data?.Token, null);
        }
        catch (Exception ex)
        {
            return (false, null, $"No se pudo conectar con el servidor: {ex.Message}");
        }
    }

    public async Task<List<PagoSPSDto>> ObtenerComprasAsync(string cedula, string token)
    {
        try
        {
            var request = new HttpRequestMessage(HttpMethod.Get, $"{_baseUrl}/ws/saludpay/compras/{cedula}");
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var response = await _httpClient.SendAsync(request);
            if (!response.IsSuccessStatusCode)
                return new List<PagoSPSDto>();

            var json = await response.Content.ReadAsStringAsync();
            var result = JsonSerializer.Deserialize<ApiResponseWrapper<List<PagoSPSDto>>>(json, _jsonOpts);
            return result?.Data ?? new List<PagoSPSDto>();
        }
        catch
        {
            return new List<PagoSPSDto>();
        }
    }

    public async Task<(bool ok, string? error)> PagarAsync(long numeroCompra, decimal valor, string token)
    {
        try
        {
            var body = JsonSerializer.Serialize(new { numeroCompra, valorPagado = valor }, _jsonOpts);
            var request = new HttpRequestMessage(HttpMethod.Post, $"{_baseUrl}/ws/saludpay/pagar")
            {
                Content = new StringContent(body, Encoding.UTF8, "application/json")
            };
            request.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);

            var response = await _httpClient.SendAsync(request);
            var json = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                var err = JsonSerializer.Deserialize<ApiResponseWrapper<object>>(json, _jsonOpts);
                return (false, err?.Message ?? "Error al procesar el pago");
            }

            return (true, null);
        }
        catch (Exception ex)
        {
            return (false, $"No se pudo conectar con el servidor: {ex.Message}");
        }
    }

    private record ApiResponseWrapper<T>(string Status, T? Data, string Message, string Timestamp);
    private record LoginData(string Token);
}
