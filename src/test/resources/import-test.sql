-- Datos de prueba cargados solo en el perfil %test (H2 en modo MySQL).
-- Admin 1-N Project 1-N Comentario

INSERT INTO administradores (admin_id, nombre, correo_electronico)
VALUES (1, 'Admin Pruebas', 'admin@portafolio.test');

INSERT INTO proyectos (proyecto_id, url_repositorio, url_imagen, descripcion, nombre, fecha_proyecto, admin_id)
VALUES (1, 'https://github.com/jairocodes/APIQuarkus', 'https://img.test/api.png', 'API REST con Quarkus', 'APIQuarkus', '2025-01-15 00:00:00', 1);
INSERT INTO proyectos (proyecto_id, url_repositorio, url_imagen, descripcion, nombre, fecha_proyecto, admin_id)
VALUES (2, 'https://github.com/jairocodes/InterfazGraficaPrimerfaces', 'https://img.test/jsf.png', 'Frontend JSF con PrimeFaces', 'InterfazGraficaPrimefaces', '2025-02-20 00:00:00', 1);

-- 12 comentarios: con paginas de 10 elementos hay al menos 2 paginas.
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (1, 'Excelente proyecto', '2025-03-01 10:00:00', 'Ana', 'ana@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (2, 'Muy buena documentacion', '2025-03-02 10:00:00', 'Luis', 'luis@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (3, 'Me gusto la arquitectura', '2025-03-03 10:00:00', 'Marta', 'marta@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (4, 'Buen uso de Panache', '2025-03-04 10:00:00', 'Pedro', 'pedro@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (5, 'Codigo limpio', '2025-03-05 10:00:00', 'Sofia', 'sofia@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (6, 'Interfaz agradable', '2025-03-06 10:00:00', 'Carlos', 'carlos@correo.test', 2);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (7, 'Buen diseno', '2025-03-07 10:00:00', 'Elena', 'elena@correo.test', 2);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (8, 'Muy intuitivo', '2025-03-08 10:00:00', 'Jorge', 'jorge@correo.test', 2);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (9, 'Gran trabajo', '2025-03-09 10:00:00', 'Lucia', 'lucia@correo.test', 2);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (10, 'Felicidades', '2025-03-10 10:00:00', 'Diego', 'diego@correo.test', 2);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (11, 'Sigue asi', '2025-03-11 10:00:00', 'Valeria', 'valeria@correo.test', 1);
INSERT INTO comentarios (comentario_id, comentario, fecha_comentario, nombre_usuario, email_usuario, id_project) VALUES (12, 'Muy util', '2025-03-12 10:00:00', 'Andres', 'andres@correo.test', 2);

-- Los ids anteriores se insertaron a mano: las secuencias de Hibernate empiezan despues
-- para que los registros creados por las pruebas no choquen con ellos.
ALTER SEQUENCE administradores_SEQ RESTART WITH 1000;
ALTER SEQUENCE proyectos_SEQ RESTART WITH 1000;
ALTER SEQUENCE comentarios_SEQ RESTART WITH 1000;
