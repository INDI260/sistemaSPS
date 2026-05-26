using System.Net.Http.Headers;
using Microsoft.AspNetCore.HttpOverrides;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddRazorPages();
var saludpayApiUrl = Environment.GetEnvironmentVariable("SALUDPAY_API_URL") ?? builder.Configuration["Api:BaseUrl"] ?? "http://localhost:5000/";
builder.Services.AddHttpClient("SaludPayApi", client =>
{
    client.BaseAddress = new Uri(saludpayApiUrl);
    client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
});

var compraApiUrl = Environment.GetEnvironmentVariable("COMPRA_API_URL") ?? builder.Configuration["CompraApi:BaseUrl"] ?? "http://frontend:80/api/compra/";
builder.Services.AddHttpClient("CompraApi", client =>
{
    client.BaseAddress = new Uri(compraApiUrl);
    client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
    client.DefaultRequestHeaders.Add("X-Internal-Key", "internal-secret-key-sps");
});

var nginxUrl = Environment.GetEnvironmentVariable("NGINX_URL") ?? builder.Configuration["Nginx:BaseUrl"] ?? "http://frontend:80/";
builder.Services.AddHttpClient("NginxApi", client =>
{
    client.BaseAddress = new Uri(nginxUrl);
    client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
});
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(30);
    options.Cookie.HttpOnly = true;
    options.Cookie.IsEssential = true;
});

var app = builder.Build();

app.UsePathBase("/saludpay");
app.UseForwardedHeaders(new ForwardedHeadersOptions
{
    ForwardedHeaders = ForwardedHeaders.XForwardedFor | ForwardedHeaders.XForwardedProto
});

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/saludpay/Error");
}
app.UseStaticFiles();
app.UseRouting();
app.UseSession();
app.UseAuthorization();
app.MapRazorPages();

app.Run();
