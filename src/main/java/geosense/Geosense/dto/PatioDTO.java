package geosense.Geosense.dto;

import java.util.List;

public record PatioDTO(
        Long id,
        List<Long> vagaIds
) {
    // Getters de compatibilidade
    public Long getId() { return id; }
    public List<Long> getVagaIds() { return vagaIds; }
}