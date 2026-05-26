using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SPS.SaludPay.Models;

[Table("pago_sps")]
public class PagoSPS
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    [MaxLength(20)]
    public string CedulaCliente { get; set; } = string.Empty;

    [Required]
    public long NumeroCompra { get; set; }

    [Required]
    [Column(TypeName = "decimal(12,2)")]
    public decimal ValorPendiente { get; set; }

    [Column(TypeName = "decimal(12,2)")]
    public decimal? ValorPagado { get; set; }

    [Required]
    [MaxLength(20)]
    public string Estado { get; set; } = "PENDIENTE";

    [Required]
    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;

    public DateTime? FechaPago { get; set; }
}
