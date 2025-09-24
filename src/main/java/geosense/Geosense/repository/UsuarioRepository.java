package geosense.Geosense.repository;

import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndSenhaAndTipo(String email, String senha, TipoUsuario tipo);

    Optional<Usuario> findByTipo(TipoUsuario tipo);

    Optional<Usuario> findByEmailOrNomeOrSenha(String email, String nome, String senha);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}

