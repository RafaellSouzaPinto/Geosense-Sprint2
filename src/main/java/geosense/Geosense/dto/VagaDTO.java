package geosense.Geosense.dto;

import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.TipoVaga;
import jakarta.validation.constraints.Min;

public record VagaDTO(
        Long id,

        @Min(value = 1, message = "Numero da vaga deve ser 1 ou maior")
        int numero,

        StatusVaga status,
        TipoVaga tipo,
        Long patioId,
        Long motoId
) {
    // Getters de compatibilidade
    public Long getId() { return id; }
    public int getNumero() { return numero; }
    public StatusVaga getStatus() { return status; }
    public TipoVaga getTipo() { return tipo; }
    public Long getPatioId() { return patioId; }
    public Long getMotoId() { return motoId; }
}
