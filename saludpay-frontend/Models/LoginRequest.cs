namespace saludpay_frontend.Models;

public class LoginRequest
{
    public string Cedula { get; set; } = string.Empty;
    public string Contrasena { get; set; } = string.Empty;
}
