package geosense.Geosense.repository;

import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VagaRepository extends JpaRepository<Vaga, Long> {
    boolean existsByNumeroAndPatioId(int numero, Long patioId);
    
    List<Vaga> findByPatioIdAndStatus(Long patioId, StatusVaga status);
    
    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.patio.id = :patioId AND v.status = :status")
    long countByPatioIdAndStatus(@Param("patioId") Long patioId, @Param("status") StatusVaga status);
    
    @Query("SELECT COUNT(v) FROM Vaga v WHERE v.patio.id = :patioId")
    long countByPatioId(@Param("patioId") Long patioId);
    
    @Query("SELECT v FROM Vaga v WHERE v.patio.id = :patioId AND v.status = 'DISPONIVEL' AND v.moto IS NULL ORDER BY v.numero")
    List<Vaga> findVagasDisponiveisByPatioId(@Param("patioId") Long patioId);
    
    List<Vaga> findByPatioIdOrderByNumeroAsc(Long patioId);

}
