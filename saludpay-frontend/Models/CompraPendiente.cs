namespace saludpay_frontend.Models;

public class CompraPendiente
{
    public int NumeroCompra { get; set; }
    public string CedulaCliente { get; set; } = string.Empty;
    public string NombreCliente { get; set; } = string.Empty;
    public decimal Valor { get; set; }
    public string Estado { get; set; } = string.Empty;
}
