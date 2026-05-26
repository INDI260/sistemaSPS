using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SPS.SaludPay.Models;

[Table("usuario_saludpay")]
public class UsuarioSaludPay
{
    [Key]
    [MaxLength(20)]
    public string Cedula { get; set; } = string.Empty;

    [Required]
    [MaxLength(200)]
    public string Nombre { get; set; } = string.Empty;

    [Required]
    [MaxLength(255)]
    public string Contrasena { get; set; } = string.Empty;

    [Required]
    [MaxLength(150)]
    public string Correo { get; set; } = string.Empty;

    [Required]
    public bool Activo { get; set; } = true;
}
