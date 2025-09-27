package geosense.Geosense.repository;

import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.AlocacaoMoto.StatusAlocacao;
import geosense.Geosense.entity.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface AlocacaoMotoRepository extends JpaRepository<AlocacaoMoto, Long> {

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findAllWithDetails();

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "WHERE a.status = 'ATIVA' " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findAlocacoesAtivas();

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.status != 'ATIVA' " +
           "ORDER BY a.dataHoraFinalizacao DESC")
    List<AlocacaoMoto> findHistoricoAlocacoes();

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "WHERE a.moto = :moto AND a.status = 'ATIVA'")
    Optional<AlocacaoMoto> findAlocacaoAtivaByMoto(@Param("moto") Moto moto);

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.moto = :moto " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findHistoricoByMoto(@Param("moto") Moto moto);

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "LEFT JOIN FETCH a.usuarioFinalizacao " +
           "WHERE a.status = :status " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByStatus(@Param("status") StatusAlocacao status);

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "WHERE v.patio.id = :patioId " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByPatioId(@Param("patioId") Long patioId);

    @Query("SELECT COUNT(a) FROM AlocacaoMoto a WHERE a.status = 'ATIVA'")
    Long countAlocacoesAtivas();

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM AlocacaoMoto a WHERE a.moto.id = :motoId AND a.status = 'ATIVA'")
    boolean existsAlocacaoAtivaByMotoId(@Param("motoId") Long motoId);

    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "WHERE a.moto = :moto AND a.status = :status " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findByMotoAndStatus(@Param("moto") Moto moto, @Param("status") StatusAlocacao status);
}
