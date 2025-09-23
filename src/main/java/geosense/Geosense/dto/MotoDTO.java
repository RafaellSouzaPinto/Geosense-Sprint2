package geosense.Geosense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotoDTO(
        Long id,

        @NotBlank(message = "Modelo e obrigatorio")
        @Size(max = 50, message = "Modelo deve ter ate 50 caracteres")
        String modelo,

        @Size(min = 7, max = 10, message = "Placa deve ter entre 7 e 10 caracteres")
        String placa,

        @Size(max = 50, message = "Chassi deve ter ate 50 caracteres")
        String chassi,

        @NotBlank(message = "Problema identificado e obrigatorio")
        String problemaIdentificado,

        Long vagaId
) {
    // Getters de compatibilidade com codigo existente
    public Long getId() { return id; }
    public String getModelo() { return modelo; }
    public String getPlaca() { return placa; }
    public String getChassi() { return chassi; }
    public String getProblemaIdentificado() { return problemaIdentificado; }
    public Long getVagaId() { return vagaId; }
}

