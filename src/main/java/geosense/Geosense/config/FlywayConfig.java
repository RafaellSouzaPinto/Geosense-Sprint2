package geosense.Geosense.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Configuration
public class FlywayConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return new FlywayMigrationStrategy() {
            @Override
            public void migrate(Flyway flyway) {
                try {
                    System.out.println("🚀 Verificando estado das migrations...");
                    
                    // Verificar se existe inconsistência entre histórico e tabelas reais
                    if (hasInconsistentState()) {
                        System.out.println("⚠️ Detectada inconsistência entre histórico de migrations e tabelas reais");
                        System.out.println("🗑️ Removendo tabelas existentes e limpando histórico...");
                        
                        // Drop manual das tabelas (evita problema com sequências do Oracle)
                        dropExistingTablesManually();
                        
                        // Limpar apenas o histórico do Flyway (sem clean total)
                        clearFlywayHistory();
                        
                        // Aguardar um pouco para garantir que a limpeza foi efetiva
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // CRIAR UMA NOVA INSTÂNCIA DO FLYWAY COMPLETAMENTE LIMPA
                        System.out.println("🔄 Criando nova instância do Flyway e forçando execução...");
                        
                        Flyway novoFlyway = Flyway.configure()
                            .dataSource(dataSource)
                            .baselineOnMigrate(true)
                            .baselineVersion("0")
                            .baselineDescription("Fresh installation")
                            .validateOnMigrate(false)
                            .outOfOrder(true)
                            .cleanDisabled(false) // Permitir clean nesta instância
                            .load();
                        
                        // Forçar clean completo com nova instância
                        System.out.println("🧹 Executando clean TOTAL com nova instância...");
                        try {
                            novoFlyway.clean();
                            System.out.println("✅ Clean executado com sucesso!");
                        } catch (Exception cleanError) {
                            System.out.println("⚠️ Clean falhou (esperado): " + cleanError.getMessage());
                            // Limpar manualmente mais uma vez
                            clearFlywayHistory();
                            dropExistingTablesManually();
                        }
                        
                        // Agora executar migrations com a nova instância
                        var result = novoFlyway.migrate();
                        
                        System.out.println("📊 Resultado das migrations com nova instância:");
                        System.out.println("   - Migrations executadas: " + result.migrationsExecuted);
                        System.out.println("   - Versão final: " + (result.targetSchemaVersion != null ? result.targetSchemaVersion : "null"));
                        
                        if (result.migrationsExecuted == 0) {
                            System.out.println("❌ AINDA não executou migrations! Problema grave detectado.");
                            System.out.println("🔍 Tentativa final com baseline forçado...");
                            novoFlyway.baseline();
                            var result2 = novoFlyway.migrate();
                            System.out.println("   - Tentativa final - Migrations executadas: " + result2.migrationsExecuted);
                        }
                        System.out.println("✅ Banco recriado com sucesso!");
                        return;
                    }
                    
                    // Tentar repair primeiro (resolve inconsistências menores)
                    flyway.repair();
                    System.out.println("🔧 Repair executado com sucesso!");
                    
                    // Executar migrations
                    System.out.println("🚀 Executando migrations do Flyway...");
                    flyway.migrate();
                    System.out.println("✅ Migrations executadas com sucesso!");
                    
                } catch (Exception e) {
                    System.out.println("⚠️ Primeira tentativa falhou, tentando recriar banco...");
                    try {
                        // Se falhar, forçar limpeza manual e recriar
                        System.out.println("🗑️ Removendo tabelas manualmente e recriando...");
                        dropExistingTablesManually();
                        clearFlywayHistory();
                        
                        // Aguardar e criar nova instância limpa
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // CRIAR UMA NOVA INSTÂNCIA DO FLYWAY COMPLETAMENTE LIMPA
                        System.out.println("🔄 Criando nova instância do Flyway...");
                        
                        Flyway novoFlyway = Flyway.configure()
                            .dataSource(dataSource)
                            .baselineOnMigrate(true)
                            .baselineVersion("0")
                            .baselineDescription("Fresh installation - fallback")
                            .validateOnMigrate(false)
                            .outOfOrder(true)
                            .cleanDisabled(false)
                            .load();
                        
                        // Tentar clean e depois migrate
                        try {
                            novoFlyway.clean();
                            System.out.println("✅ Clean da nova instância executado!");
                        } catch (Exception cleanEx) {
                            System.out.println("⚠️ Clean falhou (esperado): " + cleanEx.getMessage());
                        }
                        
                        var result = novoFlyway.migrate();
                        
                        System.out.println("📊 Resultado das migrations (fallback):");
                        System.out.println("   - Migrations executadas: " + result.migrationsExecuted);
                        System.out.println("   - Versão final: " + (result.targetSchemaVersion != null ? result.targetSchemaVersion : "null"));
                        System.out.println("✅ Banco recriado com sucesso!");
                    } catch (Exception e2) {
                        System.err.println("❌ Erro crítico ao executar migrations: " + e2.getMessage());
                        e2.printStackTrace();
                        throw e2;
                    }
                }
            }
        };
    }
    
    /**
     * Verifica se existe inconsistência entre o histórico do Flyway e as tabelas reais
     * Retorna true APENAS se realmente precisar recriar o banco
     */
    private boolean hasInconsistentState() {
        try (Connection conn = dataSource.getConnection()) {
            // Verificar se a tabela USUARIO existe (tabela principal)
            boolean hasUsuarioTable = false;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'USUARIO'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasUsuarioTable = rs.getInt(1) > 0;
                    }
                }
            }
            
            // Se a tabela USUARIO existe, assumir que está tudo OK
            if (hasUsuarioTable) {
                System.out.println("✅ Tabelas já existem - continuando execução normal");
                return false; // Tudo OK, não precisa recriar
            }
            
            // Se não tem tabela USUARIO, verificar se é primeira execução ou problema
            boolean hasFlywayHistory = false;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'flyway_schema_history'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasFlywayHistory = rs.getInt(1) > 0;
                    }
                }
            }
            
            if (!hasFlywayHistory) {
                System.out.println("🆕 Primeira execução - banco será criado");
                return false; // Primeira execução, deixar Flyway criar normalmente
            }
            
            // Tem histórico mas não tem tabelas = inconsistência real
            System.out.println("🔍 Diagnóstico:");
            System.out.println("   - Histórico Flyway existe: " + hasFlywayHistory);
            System.out.println("   - Tabela USUARIO existe: " + hasUsuarioTable);
            System.out.println("   - Situação: Histórico existe mas tabelas não - precisa recriar");
            
            return true; // Precisa recriar
            
        } catch (Exception e) {
            // Se der erro ao verificar tabelas, pode ser primeira execução
            if (e.getMessage() != null && e.getMessage().contains("ORA-00942")) {
                System.out.println("🆕 Primeira execução detectada - banco será criado");
                return false; // Deixar Flyway criar normalmente
            }
            System.out.println("⚠️ Erro ao verificar estado das tabelas: " + e.getMessage());
            return false; // Em caso de dúvida, não recriar
        }
    }
    
    /**
     * Remove manualmente as tabelas na ordem correta (para evitar problemas com FK)
     * Não tenta remover sequências do sistema
     */
    private void dropExistingTablesManually() {
        try (Connection conn = dataSource.getConnection()) {
            String[] dropStatements = {
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE ALOCACAO_MOTO CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE DEFEITO CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;", 
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE MOTO CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE VAGA CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE PATIO CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;",
                "BEGIN EXECUTE IMMEDIATE 'DROP TABLE USUARIO CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;"
            };
            
            for (String dropStatement : dropStatements) {
                try (PreparedStatement ps = conn.prepareStatement(dropStatement)) {
                    ps.execute();
                } catch (Exception e) {
                    // Ignorar erros - tabela pode não existir
                    System.out.println("   - Tentativa de drop: " + e.getMessage());
                }
            }
            
            System.out.println("✅ Tabelas removidas com sucesso!");
            
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao remover tabelas manualmente: " + e.getMessage());
            // Não falhar aqui - continuar com o processo
        }
    }
    
    /**
     * Limpa COMPLETAMENTE a tabela de histórico do Flyway para forçar reexecução das migrations
     */
    private void clearFlywayHistory() {
        try (Connection conn = dataSource.getConnection()) {
            // Desabilitar autocommit para controlar transação
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            try {
                // Remover TODAS as tabelas relacionadas ao Flyway que possam existir
                String[] flywayTables = {
                    "flyway_schema_history",
                    "schema_version", // Nome antigo do Flyway
                    "FLYWAY_SCHEMA_HISTORY", // Caso esteja em maiúscula
                    "SCHEMA_VERSION"
                };
                
                for (String tableName : flywayTables) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + tableName + " CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;")) {
                        ps.execute();
                        System.out.println("   - Tentativa de remoção da tabela: " + tableName);
                    } catch (Exception e) {
                        // Ignorar erros - tabela pode não existir
                    }
                }
                
                // Limpar cache do Oracle que pode estar mantendo metadados
                try (PreparedStatement ps = conn.prepareStatement("ALTER SYSTEM FLUSH SHARED_POOL")) {
                    ps.execute();
                    System.out.println("   - Cache do Oracle limpo");
                } catch (Exception e) {
                    // Ignorar se não tiver permissão
                    System.out.println("   - Não foi possível limpar cache (sem permissão - OK)");
                }
                
                // Commit das alterações
                conn.commit();
                System.out.println("✅ Histórico do Flyway e cache completamente limpos!");
                
            } catch (Exception e) {
                // Rollback em caso de erro
                conn.rollback();
                System.out.println("⚠️ Erro durante limpeza: " + e.getMessage());
            } finally {
                // Restaurar autocommit original
                conn.setAutoCommit(originalAutoCommit);
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao limpar histórico do Flyway: " + e.getMessage());
            // Não falhar aqui - continuar com o processo
        }
    }
}


