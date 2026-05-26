-- Crear bases de datos
CREATE DATABASE IF NOT EXISTS db_auth
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS db_salud_pay
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario para ms-auth
CREATE USER IF NOT EXISTS 'sps_auth'@'%' IDENTIFIED BY 'sps_pass';
GRANT ALL PRIVILEGES ON db_auth.* TO 'sps_auth'@'%';

-- Usuario para ms-saludpay
CREATE USER IF NOT EXISTS 'sps_saludpay'@'%' IDENTIFIED BY 'sps_pass';
GRANT ALL PRIVILEGES ON db_salud_pay.* TO 'sps_saludpay'@'%';

FLUSH PRIVILEGES;
