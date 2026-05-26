FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /app
COPY saludpay-frontend/*.csproj .
RUN dotnet restore
COPY saludpay-frontend/ .
RUN dotnet publish -c Release -o out

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/out .
EXPOSE 5001
ENV ASPNETCORE_URLS=http://0.0.0.0:5001
ENV SALUDPAY_API_URL=http://10.43.100.66:5000
ENTRYPOINT ["dotnet", "saludpay-frontend.dll"]
