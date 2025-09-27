package geosense.Geosense.repository;

import geosense.Geosense.entity.AlocacaoMoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlocacaoMotoRepository extends JpaRepository<AlocacaoMoto, Long> {
    
    @Query("SELECT a FROM AlocacaoMoto a " +
           "JOIN FETCH a.moto m " +
           "JOIN FETCH a.vaga v " +
           "JOIN FETCH v.patio p " +
           "LEFT JOIN FETCH a.mecanicoResponsavel " +
           "ORDER BY a.dataHoraAlocacao DESC")
    List<AlocacaoMoto> findAllWithDetails();
}
