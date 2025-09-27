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

    Optional<Usuario> findFirstByTipo(TipoUsuario tipo); // Buscar primeiro usuário por tipo (para compatibilidade)
    
    List<Usuario> findByTipo(TipoUsuario tipo); // Buscar lista de usuários por tipo

    Optional<Usuario> findByEmailOrNomeOrSenha(String email, String nome, String senha);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Contar usuários por tipo
    long countByTipo(TipoUsuario tipo);
    
    // Contar alocações onde o usuário é mecânico responsável
    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.mecanicoResponsavel.id = :usuarioId")
    long countAlocacoesComoMecanico(@Param("usuarioId") Long usuarioId);
    
    // Contar alocações onde o usuário é responsável pela finalização
    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.usuarioFinalizacao.id = :usuarioId")
    long countAlocacoesComoFinalizador(@Param("usuarioId") Long usuarioId);
    
    // Excluir alocações onde o usuário é mecânico responsável
    @Modifying
    @Query("DELETE FROM AlocacaoMoto a WHERE a.mecanicoResponsavel.id = :usuarioId")
    void deleteAlocacoesComoMecanico(@Param("usuarioId") Long usuarioId);
    
    // Excluir alocações onde o usuário é responsável pela finalização
    @Modifying
    @Query("DELETE FROM AlocacaoMoto a WHERE a.usuarioFinalizacao.id = :usuarioId")
    void deleteAlocacoesComoFinalizador(@Param("usuarioId") Long usuarioId);
}

