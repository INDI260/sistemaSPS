namespace saludpay_frontend.Models;

public class LoginResponse
{
    public string Token { get; set; } = string.Empty;
    public string Nombre { get; set; } = string.Empty;
    public string Cedula { get; set; } = string.Empty;
}
