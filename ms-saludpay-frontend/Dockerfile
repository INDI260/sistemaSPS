# ── Etapa 1: compilar ────────────────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /app

COPY FrontendSaludPay.csproj .
RUN dotnet restore

COPY . .
RUN dotnet publish -c Release -o /publish

# ── Etapa 2: imagen de ejecución ─────────────────────────────────────────────
FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /publish .
EXPOSE 5001
ENTRYPOINT ["dotnet", "FrontendSaludPay.dll"]
