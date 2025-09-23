package geosense.Geosense.dto;

import geosense.Geosense.entity.TiposDefeitos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DefeitoDTO(
        Long id,
        @NotNull(message = "Tipo de defeito e obrigatorio")
        TiposDefeitos tiposDefeitos,
        @NotBlank(message = "Descricao e obrigatoria")
        @Size(max = 255, message = "Descricao deve ter ate 255 caracteres")
        String descricao,
        @NotNull(message = "motoId e obrigatorio")
        Long motoId
) {
    // Getters de compatibilidade
    public Long getId() { return id; }
    public TiposDefeitos getTiposDefeitos() { return tiposDefeitos; }
    public String getDescricao() { return descricao; }
    public Long getMotoId() { return motoId; }
}
