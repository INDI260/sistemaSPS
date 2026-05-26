namespace SPS.SaludPay.Frontend.DTOs;

public record PagoSPSDto(
    long Id,
    long NumeroCompra,
    decimal ValorPendiente,
    string Estado,
    DateTime FechaCreacion);
