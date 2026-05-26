# Stage 1: Build Angular app
FROM node:20-alpine AS builder
WORKDIR /app
COPY frontend-sps/package*.json ./
RUN npm install
COPY frontend-sps/ .
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine
COPY nginx/nginx.local.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist/frontend-sps/browser /usr/share/nginx/html
EXPOSE 80
