package geosense.Geosense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import geosense.Geosense.validation.ValidPlaca;
import geosense.Geosense.validation.ValidChassi;
import geosense.Geosense.validation.ValidProblema;

public record MotoDTO(
        Long id,

        @NotBlank(message = "Modelo é obrigatório")
        @Size(max = 50, message = "Modelo deve ter até 50 caracteres")
        String modelo,

        @ValidPlaca(required = false)
        String placa,

        @ValidChassi(required = false)
        String chassi,

        @ValidProblema(required = false)
        String problemaIdentificado,

        Long vagaId
) {
    public Long getId() { return id; }
    public String getModelo() { return modelo; }
    public String getPlaca() { return placa; }
    public String getChassi() { return chassi; }
    public String getProblemaIdentificado() { return problemaIdentificado; }
    public Long getVagaId() { return vagaId; }
}

