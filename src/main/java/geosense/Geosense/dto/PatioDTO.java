package geosense.Geosense.dto;

import jakarta.validation.constraints.NotBlank;
import geosense.Geosense.validation.ValidUnidade;
import geosense.Geosense.validation.ValidCapacidade;
import java.util.List;

public record PatioDTO(
        Long id,
        
        @NotBlank(message = "Localização é obrigatória")
        String localizacao,
        
        String enderecoDetalhado,
        
        @ValidUnidade(required = true)
        String nomeUnidade,
        
        @ValidCapacidade(min = 1, max = 10000, required = true)
        Integer capacidade,
        
        List<Long> vagaIds,
        Integer vagasOcupadas,
        Integer vagasDisponiveis
) {
    public Long getId() { return id; }
    public String getLocalizacao() { return localizacao; }
    public String getEnderecoDetalhado() { return enderecoDetalhado; }
    public String getNomeUnidade() { return nomeUnidade; }
    public Integer getCapacidade() { return capacidade; }
    public List<Long> getVagaIds() { return vagaIds; }
    public Integer getVagasOcupadas() { return vagasOcupadas; }
    public Integer getVagasDisponiveis() { return vagasDisponiveis; }
}