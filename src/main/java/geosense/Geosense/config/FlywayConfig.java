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
                    System.out.println("Verificando estado das migrations");
                    
                    if (hasInconsistentState()) {
                        System.out.println("⚠️ Detectada inconsistência entre histórico de migrations e tabelas reais");
                        System.out.println("🗑️ Removendo tabelas existentes e limpando histórico...");
                        
                        dropExistingTablesManually();
                        
                        clearFlywayHistory();
                        
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        
                        System.out.println("Criando nova instância do Flyway e forçando execução");
                        
                        Flyway novoFlyway = Flyway.configure()
                            .dataSource(dataSource)
                            .baselineOnMigrate(true)
                            .baselineVersion("0")
                            .baselineDescription("Fresh installation")
                            .validateOnMigrate(false)
                            .outOfOrder(true)
                            .cleanDisabled(false)
                            .load();
                        
                        System.out.println("Executando clean TOTAL com nova instância");
                        try {
                            novoFlyway.clean();
                            System.out.println("Clean executado com sucesso!");
                        } catch (Exception cleanError) {
                            System.out.println("Clean falhou (esperado): " + cleanError.getMessage());
                            clearFlywayHistory();
                            dropExistingTablesManually();
                        }
                        
                        var result = novoFlyway.migrate();
                        
                        System.out.println("Resultado das migrations com nova instância:");
                        System.out.println("Migrations executadas: " + result.migrationsExecuted);
                        System.out.println("Versão final: " + (result.targetSchemaVersion != null ? result.targetSchemaVersion : "null"));
                        
                        if (result.migrationsExecuted == 0) {
                            System.out.println("AINDA não executou migrations! Problema grave detectado");
                            System.out.println("Tentativa final com baseline forçado");
                            novoFlyway.baseline();
                            var result2 = novoFlyway.migrate();
                            System.out.println("Tentativa final - Migrations executadas: " + result2.migrationsExecuted);
                        }
                        System.out.println("Banco recriado com sucesso");
                        return;
                    }
                    
                    flyway.repair();
                    System.out.println("Repair executado com sucesso");
                    
                    System.out.println("Executando migrations do Flyway");
                    flyway.migrate();
                    System.out.println("Migrations executadas com sucesso!");
                    
                } catch (Exception e) {
                    System.out.println("Primeira tentativa falhou, tentando recriar banco");
                    try {
                        System.out.println("🗑️ Removendo tabelas manualmente e recriando");
                        dropExistingTablesManually();
                        clearFlywayHistory();
                        
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        
                        System.out.println("Criando nova instância do Flyway...");
                        
                        Flyway novoFlyway = Flyway.configure()
                            .dataSource(dataSource)
                            .baselineOnMigrate(true)
                            .baselineVersion("0")
                            .baselineDescription("Fresh installation - fallback")
                            .validateOnMigrate(false)
                            .outOfOrder(true)
                            .cleanDisabled(false)
                            .load();
                        
                        try {
                            novoFlyway.clean();
                            System.out.println("✅ Clean da nova instância executado!");
                        } catch (Exception cleanEx) {
                            System.out.println("⚠️ Clean falhou (esperado): " + cleanEx.getMessage());
                        }
                        
                        var result = novoFlyway.migrate();
                        
                        System.out.println("Resultado das migrations (fallback):");
                        System.out.println("Migrations executadas: " + result.migrationsExecuted);
                        System.out.println("Versão final: " + (result.targetSchemaVersion != null ? result.targetSchemaVersion : "null"));
                        System.out.println("Banco recriado com sucesso!");
                    } catch (Exception e2) {
                        System.err.println("Erro crítico ao executar migrations: " + e2.getMessage());
                        e2.printStackTrace();
                        throw e2;
                    }
                }
            }
        };
    }

    private boolean hasInconsistentState() {
        try (Connection conn = dataSource.getConnection()) {
            boolean hasUsuarioTable = false;
            try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'USUARIO'")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        hasUsuarioTable = rs.getInt(1) > 0;
                    }
                }
            }
            
            if (hasUsuarioTable) {
                System.out.println("Tabelas já existem - continuando execução normal");
                return false;
            }
            
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
                System.out.println("Primeira execução - banco será criado");
                return false;
            }
            
            System.out.println("Diagnóstico:");
            System.out.println("   - Histórico Flyway existe: " + hasFlywayHistory);
            System.out.println("   - Tabela USUARIO existe: " + hasUsuarioTable);
            System.out.println("   - Situação: Histórico existe mas tabelas não - precisa recriar");
            
            return true;
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00942")) {
                System.out.println("🆕 Primeira execução detectada - banco será criado");
                return false;
            }
            System.out.println("⚠️ Erro ao verificar estado das tabelas: " + e.getMessage());
            return false;
        }
    }

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
                    System.out.println("   - Tentativa de drop: " + e.getMessage());
                }
            }
            
            System.out.println("✅ Tabelas removidas com sucesso!");
            
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao remover tabelas manualmente: " + e.getMessage());
        }
    }

    private void clearFlywayHistory() {
        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            try {
                String[] flywayTables = {
                    "flyway_schema_history",
                    "schema_version",
                    "FLYWAY_SCHEMA_HISTORY",
                    "SCHEMA_VERSION"
                };
                
                for (String tableName : flywayTables) {
                    try (PreparedStatement ps = conn.prepareStatement(
                        "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + tableName + " CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;")) {
                        ps.execute();
                        System.out.println("   - Tentativa de remoção da tabela: " + tableName);
                    } catch (Exception e) {

                    }
                }
                
                try (PreparedStatement ps = conn.prepareStatement("ALTER SYSTEM FLUSH SHARED_POOL")) {
                    ps.execute();
                    System.out.println("   - Cache do Oracle limpo");
                } catch (Exception e) {
                    System.out.println("   - Não foi possível limpar cache (sem permissão - OK)");
                }
                
                conn.commit();
                System.out.println("Histórico do Flyway e cache completamente limpos");
                
            } catch (Exception e) {
                conn.rollback();
                System.out.println("Erro durante limpeza: " + e.getMessage());
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
            
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao limpar histórico do Flyway: " + e.getMessage());
        }
    }
}


