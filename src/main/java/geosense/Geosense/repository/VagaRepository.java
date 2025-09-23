package geosense.Geosense.repository;

import geosense.Geosense.entity.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VagaRepository extends JpaRepository<Vaga, Long> {
    boolean existsByNumeroAndPatioId(int numero, Long patioId);

}
