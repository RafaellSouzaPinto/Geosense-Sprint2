-- V3: Seed initial data (idempotent, Oracle-friendly)

-- Admin default (senha em texto por enquanto; substituir por hash quando Security estiver pronto)
DECLARE n NUMBER; BEGIN
  SELECT COUNT(*) INTO n FROM USUARIO WHERE EMAIL = 'mottu@gmail.com';
  IF n = 0 THEN
    INSERT INTO USUARIO (NOME, EMAIL, SENHA, TIPO)
    VALUES ('Administrador', 'mottu@gmail.com', 'Geosense@2025', 'ADMINISTRADOR');
  END IF;
END;
/

-- Pátio inicial (cria 1 pátio se ainda não existir nenhum)
DECLARE n NUMBER; BEGIN
  SELECT COUNT(*) INTO n FROM PATIO;
  IF n = 0 THEN
    INSERT INTO PATIO (ID) VALUES (DEFAULT);
  END IF;
END;
/

-- Vagas (associa ao primeiro pátio existente)
DECLARE n NUMBER; v_patio_id NUMBER; BEGIN
  SELECT id INTO v_patio_id FROM PATIO WHERE ROWNUM = 1;
  SELECT COUNT(*) INTO n FROM VAGA WHERE NUMERO = 1;
  IF n = 0 THEN
    INSERT INTO VAGA (NUMERO, STATUS, TIPO, PATIO_ID)
    VALUES (1, 'DISPONIVEL', 'REPARO_SIMPLES', v_patio_id);
  END IF;
  SELECT COUNT(*) INTO n FROM VAGA WHERE NUMERO = 2;
  IF n = 0 THEN
    INSERT INTO VAGA (NUMERO, STATUS, TIPO, PATIO_ID)
    VALUES (2, 'DISPONIVEL', 'MOTOR_DEFEITUOSO', v_patio_id);
  END IF;
END;
/

-- Motos exemplo (sem alocar vaga por enquanto)
DECLARE n NUMBER; BEGIN
  SELECT COUNT(*) INTO n FROM MOTO WHERE PLACA = 'ABC1D23';
  IF n = 0 THEN
    INSERT INTO MOTO (MODELO, PLACA, CHASSI, PROBLEMA_IDENTIFICADO, VAGA_ID)
    VALUES ('Honda CG 160', 'ABC1D23', 'CHS123', 'Revisao geral', NULL);
  END IF;
END;
/

-- Defeito exemplo para a moto acima
DECLARE n NUMBER; v_moto_id NUMBER; BEGIN
  SELECT ID INTO v_moto_id FROM MOTO WHERE PLACA = 'ABC1D23' AND ROWNUM = 1;
  SELECT COUNT(*) INTO n FROM DEFEITO WHERE DESCRICAO = 'Troca de oleo e filtros';
  IF n = 0 THEN
    INSERT INTO DEFEITO (TIPOS_DEFEITOS, DESCRICAO, MOTO_ID)
    VALUES ('REPAROS_SIMPLES', 'Troca de oleo e filtros', v_moto_id);
  END IF;
END;
/

