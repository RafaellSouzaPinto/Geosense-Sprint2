-- V15: Corrigir e consistir dados existentes de alocações
-- Esta migração garante que os dados existentes estejam consistentes
-- com a nova estrutura de controle de histórico

-- Corrigir alocações duplicadas (Oracle PL/SQL)
DECLARE
    contador NUMBER := 0;
    v_count NUMBER;
    v_latest_id NUMBER;
BEGIN
    -- Log do início do processo
    DBMS_OUTPUT.PUT_LINE('Iniciando correção de dados existentes de alocações...');
    
    -- Processar alocações duplicadas para cada moto
    FOR moto_rec IN (
        SELECT MOTO_ID, COUNT(*) as qtd_alocacoes
        FROM ALOCACAO_MOTO 
        WHERE STATUS = 'ATIVA'
        GROUP BY MOTO_ID
        HAVING COUNT(*) > 1
    ) LOOP
        -- Encontrar a alocação mais recente para manter ativa
        SELECT ID INTO v_latest_id
        FROM (
            SELECT ID 
            FROM ALOCACAO_MOTO 
            WHERE MOTO_ID = moto_rec.MOTO_ID AND STATUS = 'ATIVA'
            ORDER BY DATA_HORA_ALOCACAO DESC
        ) WHERE ROWNUM = 1;
        
        -- Marcar as outras como REALOCADA
        UPDATE ALOCACAO_MOTO 
        SET STATUS = 'REALOCADA',
            DATA_HORA_FINALIZACAO = DATA_HORA_ALOCACAO + INTERVAL '1' MINUTE,
            MOTIVO_FINALIZACAO = 'Alocação substituída durante migração de dados'
        WHERE MOTO_ID = moto_rec.MOTO_ID 
          AND STATUS = 'ATIVA' 
          AND ID != v_latest_id;
        
        contador := contador + SQL%ROWCOUNT;
        
        DBMS_OUTPUT.PUT_LINE('Moto ' || moto_rec.MOTO_ID || ': ' || (moto_rec.qtd_alocacoes - 1) || ' alocações marcadas como REALOCADA');
    END LOOP;
    
    DBMS_OUTPUT.PUT_LINE('Processadas ' || contador || ' alocações. Correção concluída!');
END;
/

-- Verificar consistência dos relacionamentos
-- Garantir que motos com vaga tenham alocação ativa
UPDATE MOTO 
SET VAGA_ID = NULL 
WHERE ID IN (
    SELECT m.ID 
    FROM MOTO m 
    LEFT JOIN ALOCACAO_MOTO a ON (m.ID = a.MOTO_ID AND a.STATUS = 'ATIVA')
    WHERE m.VAGA_ID IS NOT NULL AND a.ID IS NULL
);

-- Garantir que vagas ocupadas tenham moto alocada
UPDATE VAGA 
SET STATUS = 'DISPONIVEL' 
WHERE STATUS = 'OCUPADA' AND ID NOT IN (
    SELECT DISTINCT m.VAGA_ID 
    FROM MOTO m
    JOIN ALOCACAO_MOTO a ON (m.ID = a.MOTO_ID AND a.STATUS = 'ATIVA')
    WHERE m.VAGA_ID IS NOT NULL
);

-- Verificar e reportar estatísticas finais (Oracle PL/SQL)
DECLARE
    total_alocacoes NUMBER;
    alocacoes_ativas NUMBER;
    alocacoes_realocadas NUMBER;
    alocacoes_finalizadas NUMBER;
BEGIN
    SELECT COUNT(*) INTO total_alocacoes FROM ALOCACAO_MOTO;
    SELECT COUNT(*) INTO alocacoes_ativas FROM ALOCACAO_MOTO WHERE STATUS = 'ATIVA';
    SELECT COUNT(*) INTO alocacoes_realocadas FROM ALOCACAO_MOTO WHERE STATUS = 'REALOCADA';
    SELECT COUNT(*) INTO alocacoes_finalizadas FROM ALOCACAO_MOTO WHERE STATUS = 'FINALIZADA';
    
    DBMS_OUTPUT.PUT_LINE('=== ESTATÍSTICAS PÓS-MIGRAÇÃO ===');
    DBMS_OUTPUT.PUT_LINE('Total de alocações: ' || total_alocacoes);
    DBMS_OUTPUT.PUT_LINE('Alocações ativas: ' || alocacoes_ativas);
    DBMS_OUTPUT.PUT_LINE('Alocações realocadas: ' || alocacoes_realocadas);
    DBMS_OUTPUT.PUT_LINE('Alocações finalizadas: ' || alocacoes_finalizadas);
    DBMS_OUTPUT.PUT_LINE('==================================');
END;
/
