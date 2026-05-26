namespace SPS.SaludPay.DTOs;

public record PagoSPSDto(
    long Id,
    long NumeroCompra,
    decimal ValorPendiente,
    string Estado,
    DateTime FechaCreacion);
