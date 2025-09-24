package geosense.Geosense.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AlocacaoMotoDTO(
        Long id,
        @NotNull(message = "motoId e obrigatorio")
        Long motoId,
        @NotNull(message = "vagaId e obrigatorio")
        Long vagaId,
        Long mecanicoResponsavelId,
        LocalDateTime dataHoraAlocacao
) {
    // Getters de compatibilidade
    public Long getId() { return id; }
    public Long getMotoId() { return motoId; }
    public Long getVagaId() { return vagaId; }
    public Long getMecanicoResponsavelId() { return mecanicoResponsavelId; }
    public LocalDateTime getDataHoraAlocacao() { return dataHoraAlocacao; }
}
