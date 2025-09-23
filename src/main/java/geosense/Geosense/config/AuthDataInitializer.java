package geosense.Geosense.config;

import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthDataInitializer {

    @Bean
    public CommandLineRunner initAdminUser(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            repo.findByTipo(TipoUsuario.ADMINISTRADOR).ifPresentOrElse(
                    u -> {
                        if (!u.getSenha().startsWith("$2a") && !u.getSenha().startsWith("$2b") && !u.getSenha().startsWith("$2y")) {
                            u.setSenha(encoder.encode(u.getSenha()));
                            repo.save(u);
                        }
                    },
                    () -> {
                        Usuario admin = new Usuario();
                        admin.setNome("Administrador");
                        admin.setEmail("mottu@gmail.com");
                        admin.setSenha(encoder.encode("Geosense@2025"));
                        admin.setTipo(TipoUsuario.ADMINISTRADOR);
                        repo.save(admin);
                    }
            );
        };
    }
}


