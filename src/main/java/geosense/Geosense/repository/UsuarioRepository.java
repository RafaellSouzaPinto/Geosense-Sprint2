package geosense.Geosense.repository;

import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailAndSenhaAndTipo(String email, String senha, TipoUsuario tipo);

    Optional<Usuario> findFirstByTipo(TipoUsuario tipo);
    
    List<Usuario> findByTipo(TipoUsuario tipo);

    Optional<Usuario> findByEmailOrNomeOrSenha(String email, String nome, String senha);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByTipo(TipoUsuario tipo);
    
    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.mecanicoResponsavel.id = :usuarioId")
    long countAlocacoesComoMecanico(@Param("usuarioId") Long usuarioId);
    
    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.usuarioFinalizacao.id = :usuarioId")
    long countAlocacoesComoFinalizador(@Param("usuarioId") Long usuarioId);
    
    @Modifying
    @Query("DELETE FROM AlocacaoMoto a WHERE a.mecanicoResponsavel.id = :usuarioId")
    void deleteAlocacoesComoMecanico(@Param("usuarioId") Long usuarioId);
    
    @Modifying
    @Query("DELETE FROM AlocacaoMoto a WHERE a.usuarioFinalizacao.id = :usuarioId")
    void deleteAlocacoesComoFinalizador(@Param("usuarioId") Long usuarioId);
}

