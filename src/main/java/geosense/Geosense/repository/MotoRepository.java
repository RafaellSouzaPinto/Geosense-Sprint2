package geosense.Geosense.repository;

import geosense.Geosense.entity.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MotoRepository extends JpaRepository<Moto, Long> {
    
    boolean existsByVagaId(Long vagaId);
    
    @Query("SELECT m FROM Moto m WHERE m.vaga IS NOT NULL")
    List<Moto> findMotosComVaga();
    
    @Query("SELECT m FROM Moto m WHERE m.vaga IS NULL")
    List<Moto> findMotosSemVaga();
}
