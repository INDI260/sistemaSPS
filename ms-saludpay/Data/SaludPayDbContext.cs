using Microsoft.EntityFrameworkCore;
using SPS.SaludPay.Models;

namespace SPS.SaludPay.Data;

public class SaludPayDbContext : DbContext
{
    public SaludPayDbContext(DbContextOptions<SaludPayDbContext> options) : base(options) { }

    public DbSet<PagoSPS> PagosSPS { get; set; }
    public DbSet<UsuarioSaludPay> UsuariosSaludPay { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        modelBuilder.Entity<PagoSPS>()
            .HasIndex(p => p.NumeroCompra)
            .IsUnique();
    }
}
