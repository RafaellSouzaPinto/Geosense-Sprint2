-- V8: Ensure unique constraint/index for USUARIO.EMAIL (Oracle-safe)

DECLARE n NUMBER; t NUMBER; BEGIN
  SELECT COUNT(*) INTO t FROM user_tables WHERE table_name = 'USUARIO';
  IF t > 0 THEN
    SELECT COUNT(*) INTO n FROM user_indexes WHERE index_name = 'UK_USUARIO_EMAIL';
    IF n = 0 THEN EXECUTE IMMEDIATE 'CREATE UNIQUE INDEX UK_USUARIO_EMAIL ON USUARIO (EMAIL)'; END IF;
  END IF;
END;
/


