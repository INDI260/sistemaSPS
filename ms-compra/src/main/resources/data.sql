INSERT IGNORE INTO plan_salud (codigo_plan, nombre_plan, descripcion, precio, activo) VALUES
('PLAN-001', 'Plan Básico', 'Plan de salud básico con consultas y exámenes', 150000.00, true),
('PLAN-002', 'Plan Plus', 'Plan de salud plus con hospitalización y especialistas', 350000.00, true),
('PLAN-003', 'Plan Premium', 'Plan de salud premium con cirugía y atención preventiva', 600000.00, true);

INSERT IGNORE INTO servicio_medico (codigo_servicio, nombre, descripcion, tipo, precio, codigo_plan) VALUES
('SERV-001', 'Consulta General', 'Consulta médica general', 'CONSULTA', 50000.00, 'PLAN-001'),
('SERV-002', 'Examen de Sangre', 'Análisis de sangre completo', 'EXAMEN', 100000.00, 'PLAN-001'),
('SERV-003', 'Hospitalización', 'Servicio de hospitalización', 'HOSPITALIZACION', 200000.00, 'PLAN-002'),
('SERV-004', 'Consulta Especialista', 'Consulta con médico especialista', 'CONSULTA', 150000.00, 'PLAN-002'),
('SERV-005', 'Cirugía Ambulatoria', 'Procedimiento quirúrgico ambulatorio', 'EXAMEN', 400000.00, 'PLAN-003'),
('SERV-006', 'Consulta Preventiva', 'Consulta médica preventiva', 'CONSULTA', 200000.00, 'PLAN-003');
