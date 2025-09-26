package geosense.Geosense.repository;

import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndSenhaAndTipo(String email, String senha, TipoUsuario tipo);

    Optional<Usuario> findFirstByTipo(TipoUsuario tipo); // Buscar primeiro usuário por tipo (para compatibilidade)
    
    List<Usuario> findByTipo(TipoUsuario tipo); // Buscar lista de usuários por tipo

    Optional<Usuario> findByEmailOrNomeOrSenha(String email, String nome, String senha);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Contar usuários por tipo
    long countByTipo(TipoUsuario tipo);
}

