# NGINX - Servidor Proxy del Sistema SPS

## Requisitos

- NGINX 1.25+
- Certificado SSL (generar con `ssl/generar-cert.sh`)

## Despliegue

### 1. Generar certificado SSL (solo primera vez)

```bash
sudo bash ssl/generar-cert.sh
```

### 2. Construir Angular

```bash
cd ../frontend-sps
npx ng build
```

### 3. Copiar archivos estáticos

```bash
sudo cp -r dist/frontend-sps/browser/* /var/www/frontend-sps/
```

### 4. Probar configuración NGINX

```bash
sudo nginx -t -c /etc/nginx/nginx.conf
```

### 5. Iniciar NGINX

```bash
sudo nginx
```

### 6. Verificar

Abrir `https://localhost` en el navegador.

## Rutas proxy

| Ruta            | Destino                      |
|-----------------|------------------------------|
| `/`             | Archivos estáticos Angular   |
| `/api/auth/`    | http://localhost:8081/ws/auth/ |
| `/api/compra/`  | http://compra_backend/ws/compra/ |
| `/saludpay/`    | http://localhost:5001/       |

## Balanceo de carga

El upstream `compra_backend` balancea peticiones a ms-compra.
Para agregar otra instancia, descomentar la línea en `nginx.conf`.
