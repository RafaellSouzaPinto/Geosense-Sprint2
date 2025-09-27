package geosense.Geosense.config;

import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Inicializador do usuário administrador
 * Garante que sempre exista um admin no sistema
 */
@Component
@DependsOn("flyway")
public class AdminInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "mottu@gmail.com";
    private static final String ADMIN_PASSWORD = "Geosense@2025";
    private static final String ADMIN_NAME = "Administrador Geral";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Aguardar um pouco para garantir que as migrations do Flyway terminaram
        Thread.sleep(2000);
        
        try {
            createAdminIfNotExists();
        } catch (Exception e) {
            // Se falhar, significa que as tabelas ainda não existem ou há problema de sincronização
            // O usuário admin será criado pela migration V3__seed_data.sql ou V10__reset_complete.sql
            System.out.println("⚠️ Erro ao verificar/criar usuário admin via código: " + e.getMessage());
            System.out.println("✅ Usuário admin será criado pelas migrations do banco de dados");
            
            // Log detalhado apenas em caso de erro inesperado
            if (!(e.getMessage() != null && (
                e.getMessage().contains("ORA-00942") || 
                e.getMessage().contains("table or view does not exist") ||
                e.getMessage().contains("Table") ||
                e.getMessage().contains("USUARIO")
            ))) {
                System.out.println("🔍 Detalhes do erro inesperado:");
                e.printStackTrace();
            }
        }
    }

    private void createAdminIfNotExists() {
        // Verificar se já existe um admin com o email específico
        Optional<Usuario> existingAdmin = usuarioRepository.findByEmail(ADMIN_EMAIL);
        
        if (existingAdmin.isEmpty()) {
            // Criar o usuário administrador
            Usuario admin = new Usuario();
            admin.setNome(ADMIN_NAME);
            admin.setEmail(ADMIN_EMAIL);
            admin.setSenha(passwordEncoder.encode(ADMIN_PASSWORD)); // Senha criptografada
            admin.setTipo(TipoUsuario.ADMINISTRADOR);
            
            usuarioRepository.save(admin);
            
            System.out.println("✅ Usuário administrador criado com sucesso!");
            System.out.println("   📧 Email: " + ADMIN_EMAIL);
            System.out.println("   🔑 Senha: " + ADMIN_PASSWORD);
            System.out.println("   👤 Nome: " + ADMIN_NAME);
        } else {
            Usuario admin = existingAdmin.get();
            
            // Verificar se o admin existente tem a senha correta (pode ter sido alterada)
            if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getSenha())) {
                admin.setSenha(passwordEncoder.encode(ADMIN_PASSWORD));
                usuarioRepository.save(admin);
                System.out.println("🔄 Senha do administrador atualizada!");
            }
            
            // Garantir que é do tipo ADMINISTRADOR
            if (admin.getTipo() != TipoUsuario.ADMINISTRADOR) {
                admin.setTipo(TipoUsuario.ADMINISTRADOR);
                usuarioRepository.save(admin);
                System.out.println("🔄 Tipo do usuário atualizado para ADMINISTRADOR!");
            }
            
            System.out.println("✅ Usuário administrador já existe e está configurado corretamente!");
        }
    }
}
