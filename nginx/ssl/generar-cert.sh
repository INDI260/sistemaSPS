#!/bin/bash
# Genera certificado autofirmado para desarrollo
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/nginx/ssl/key.pem \
    -out /etc/nginx/ssl/cert.pem \
    -subj "/C=CO/ST=Estado/L=Ciudad/O=SPS/CN=localhost"

echo "Certificado autofirmado generado en /etc/nginx/ssl/"
