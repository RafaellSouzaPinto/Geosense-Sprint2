package geosense.Geosense.repository;

import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.AlocacaoMoto.StatusAlocacao;
import geosense.Geosense.entity.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository melhorado para AlocacaoMoto
 * Inclui consultas específicas para controle de histórico e status
 */
public interface AlocacaoMotoRepository extends JpaRepository<AlocacaoMoto, Long> {
    
    /**
     * Busca todas as alocações com detalhes completos, ordenadas por data
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findAllWithDetails();
    
    /**
     * Busca apenas alocações ativas (em uso atualmente)
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "WHERE a.status = 'ATIVA' " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findAlocacoesAtivas();
    
    /**
     * Busca histórico completo de alocações (incluindo finalizadas)
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.status != 'ATIVA' " +
           "ORDER BY a.dataHoraFinalizacao DESC")
    List<AlocacaoMoto> findHistoricoAlocacoes();
    
    /**
     * Busca alocação ativa de uma moto específica
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "WHERE a.moto = :moto AND a.status = 'ATIVA'")
    Optional<AlocacaoMoto> findAlocacaoAtivaByMoto(@Param("moto") Moto moto);
    
    /**
     * Busca histórico de alocações de uma moto específica
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.moto = :moto " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findHistoricoByMoto(@Param("moto") Moto moto);
    
    /**
     * Busca alocações por status específico
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.status = :status " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByStatus(@Param("status") StatusAlocacao status);
    
    /**
     * Busca alocações de um pátio específico
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "WHERE v.patio.id = :patioId " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByPatioId(@Param("patioId") Long patioId);
    
    /**
     * Conta quantas motos estão atualmente alocadas
     */
    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.status = 'ATIVA'")
    Long countAlocacoesAtivas();
    
    /**
     * Verifica se uma moto tem alocação ativa
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM AlocacaoMoto a WHERE a.moto.id = :motoId AND a.status = 'ATIVA'")
    boolean existsAlocacaoAtivaByMotoId(@Param("motoId") Long motoId);
    
    /**
     * Busca alocações de uma moto específica por status
     */
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "WHERE a.moto = :moto AND a.status = :status " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByMotoAndStatus(@Param("moto") Moto moto, @Param("status") StatusAlocacao status);
}
