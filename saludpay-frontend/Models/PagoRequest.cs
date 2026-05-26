namespace saludpay_frontend.Models;

public class PagoRequest
{
    public int NumeroCompra { get; set; }
    public decimal ValorPagado { get; set; }
}

public class PagoResponse
{
    public bool Exitoso { get; set; }
    public string Mensaje { get; set; } = string.Empty;
}
