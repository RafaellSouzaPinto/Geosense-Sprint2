-- V11: Corrigir enum TipoUsuario - substituir ADMIN por ADMINISTRADOR

UPDATE USUARIO SET TIPO = 'ADMINISTRADOR' WHERE TIPO = 'ADMIN';

MERGE INTO USUARIO u
USING (SELECT 'Admin' as nome, 'admin@geosense.com' as email, '$2a$10$D4CqT4GWL.NBIlrOxNwLIOY3X1xPTVLR5bKCkPjGtQjV1b.QsWyTu' as senha, 'ADMINISTRADOR' as tipo FROM DUAL) dados
ON (u.EMAIL = dados.email)
WHEN NOT MATCHED THEN
  INSERT (NOME, EMAIL, SENHA, TIPO) VALUES (dados.nome, dados.email, dados.senha, dados.tipo);

MERGE INTO USUARIO u
USING (SELECT 'Mecânico João' as nome, 'joao@geosense.com' as email, '$2a$10$D4CqT4GWL.NBIlrOxNwLIOY3X1xPTVLR5bKCkPjGtQjV1b.QsWyTu' as senha, 'MECANICO' as tipo FROM DUAL) dados
ON (u.EMAIL = dados.email)
WHEN NOT MATCHED THEN
  INSERT (NOME, EMAIL, SENHA, TIPO) VALUES (dados.nome, dados.email, dados.senha, dados.tipo);

COMMIT;
