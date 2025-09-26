package geosense.Geosense.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO simplificado para Alocação de Moto
 * Conceito: Colocar uma MOTO numa VAGA de um PÁTIO
 */
public class AlocacaoMotoDTO {
    
    private Long id;
    
    @NotNull(message = "Selecione uma moto")
    private Long motoId;
    
    @NotNull(message = "Selecione um pátio")
    private Long patioId;
    
    @NotNull(message = "Selecione uma vaga")
    private Long vagaId;
    
    private Long mecanicoId;
    private String observacoes;
    
    // Construtores
    public AlocacaoMotoDTO() {}
    
    public AlocacaoMotoDTO(Long id, Long motoId, Long patioId, Long vagaId, Long mecanicoId, String observacoes) {
        this.id = id;
        this.motoId = motoId;
        this.patioId = patioId;
        this.vagaId = vagaId;
        this.mecanicoId = mecanicoId;
        this.observacoes = observacoes;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMotoId() { return motoId; }
    public void setMotoId(Long motoId) { this.motoId = motoId; }
    
    public Long getPatioId() { return patioId; }
    public void setPatioId(Long patioId) { this.patioId = patioId; }
    
    public Long getVagaId() { return vagaId; }
    public void setVagaId(Long vagaId) { this.vagaId = vagaId; }
    
    public Long getMecanicoId() { return mecanicoId; }
    public void setMecanicoId(Long mecanicoId) { this.mecanicoId = mecanicoId; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}