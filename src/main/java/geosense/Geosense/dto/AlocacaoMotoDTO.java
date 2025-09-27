package geosense.Geosense.dto;

import jakarta.validation.constraints.NotNull;
import geosense.Geosense.entity.AlocacaoMoto.StatusAlocacao;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DTO melhorado para Alocação de Moto
 * Inclui controle de status e histórico completo das alocações
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
    
    // Novos campos para controle de histórico
    private LocalDateTime dataHoraAlocacao;
    private LocalDateTime dataHoraFinalizacao;
    private StatusAlocacao status;
    private String motivoFinalizacao;
    private Long usuarioFinalizacaoId;
    
    // Campos extras para exibição
    private String motoInfo;        // Ex: "CB 600F - ABC1234"
    private String patioInfo;       // Ex: "Pátio Central"
    private String vagaInfo;        // Ex: "Vaga 15"
    private String mecanicoInfo;    // Ex: "João Silva"
    private String usuarioFinalizacaoInfo; // Ex: "Maria Santos"
    private String duracaoFormatada; // Ex: "2 dias, 3 horas"
    
    // Construtores
    public AlocacaoMotoDTO() {}
    
    // Construtor básico (compatibilidade com código existente)
    public AlocacaoMotoDTO(Long id, Long motoId, Long patioId, Long vagaId, Long mecanicoId, String observacoes) {
        this.id = id;
        this.motoId = motoId;
        this.patioId = patioId;
        this.vagaId = vagaId;
        this.mecanicoId = mecanicoId;
        this.observacoes = observacoes;
    }
    
    // Construtor completo com status e histórico
    public AlocacaoMotoDTO(Long id, Long motoId, Long patioId, Long vagaId, Long mecanicoId, String observacoes,
                          LocalDateTime dataHoraAlocacao, LocalDateTime dataHoraFinalizacao, StatusAlocacao status,
                          String motivoFinalizacao, Long usuarioFinalizacaoId) {
        this.id = id;
        this.motoId = motoId;
        this.patioId = patioId;
        this.vagaId = vagaId;
        this.mecanicoId = mecanicoId;
        this.observacoes = observacoes;
        this.dataHoraAlocacao = dataHoraAlocacao;
        this.dataHoraFinalizacao = dataHoraFinalizacao;
        this.status = status;
        this.motivoFinalizacao = motivoFinalizacao;
        this.usuarioFinalizacaoId = usuarioFinalizacaoId;
    }
    
    // Construtor para exibição com informações formatadas
    public AlocacaoMotoDTO(Long id, String motoInfo, String patioInfo, String vagaInfo, String mecanicoInfo,
                          LocalDateTime dataHoraAlocacao, LocalDateTime dataHoraFinalizacao, StatusAlocacao status,
                          String observacoes, String motivoFinalizacao, String usuarioFinalizacaoInfo) {
        this.id = id;
        this.motoInfo = motoInfo;
        this.patioInfo = patioInfo;
        this.vagaInfo = vagaInfo;
        this.mecanicoInfo = mecanicoInfo;
        this.dataHoraAlocacao = dataHoraAlocacao;
        this.dataHoraFinalizacao = dataHoraFinalizacao;
        this.status = status;
        this.observacoes = observacoes;
        this.motivoFinalizacao = motivoFinalizacao;
        this.usuarioFinalizacaoInfo = usuarioFinalizacaoInfo;
        this.duracaoFormatada = calcularDuracao();
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

    // Novos getters e setters
    public LocalDateTime getDataHoraAlocacao() { return dataHoraAlocacao; }
    public void setDataHoraAlocacao(LocalDateTime dataHoraAlocacao) { this.dataHoraAlocacao = dataHoraAlocacao; }

    public LocalDateTime getDataHoraFinalizacao() { return dataHoraFinalizacao; }
    public void setDataHoraFinalizacao(LocalDateTime dataHoraFinalizacao) { this.dataHoraFinalizacao = dataHoraFinalizacao; }

    public StatusAlocacao getStatus() { return status; }
    public void setStatus(StatusAlocacao status) { this.status = status; }

    public String getMotivoFinalizacao() { return motivoFinalizacao; }
    public void setMotivoFinalizacao(String motivoFinalizacao) { this.motivoFinalizacao = motivoFinalizacao; }

    public Long getUsuarioFinalizacaoId() { return usuarioFinalizacaoId; }
    public void setUsuarioFinalizacaoId(Long usuarioFinalizacaoId) { this.usuarioFinalizacaoId = usuarioFinalizacaoId; }

    // Campos para exibição
    public String getMotoInfo() { return motoInfo; }
    public void setMotoInfo(String motoInfo) { this.motoInfo = motoInfo; }

    public String getPatioInfo() { return patioInfo; }
    public void setPatioInfo(String patioInfo) { this.patioInfo = patioInfo; }

    public String getVagaInfo() { return vagaInfo; }
    public void setVagaInfo(String vagaInfo) { this.vagaInfo = vagaInfo; }

    public String getMecanicoInfo() { return mecanicoInfo; }
    public void setMecanicoInfo(String mecanicoInfo) { this.mecanicoInfo = mecanicoInfo; }

    public String getUsuarioFinalizacaoInfo() { return usuarioFinalizacaoInfo; }
    public void setUsuarioFinalizacaoInfo(String usuarioFinalizacaoInfo) { this.usuarioFinalizacaoInfo = usuarioFinalizacaoInfo; }

    public String getDuracaoFormatada() { 
        return duracaoFormatada != null ? duracaoFormatada : calcularDuracao(); 
    }
    public void setDuracaoFormatada(String duracaoFormatada) { this.duracaoFormatada = duracaoFormatada; }
    
    public String duracaoFormatada() {
        return getDuracaoFormatada();
    }

    // Métodos utilitários
    public boolean isAtiva() {
        return status == StatusAlocacao.ATIVA;
    }

    public boolean isFinalizada() {
        return status != null && status != StatusAlocacao.ATIVA;
    }

    public String getStatusFormatado() {
        if (status == null) return "N/A";
        switch (status) {
            case ATIVA: return "Ativa";
            case REALOCADA: return "Realocada";
            case FINALIZADA: return "Finalizada";
            case CANCELADA: return "Cancelada";
            default: return status.toString();
        }
    }
    
    public String statusFormatado() {
        return getStatusFormatado();
    }
    
    public String getIconeStatus() {
        if (status == null) return "fa-question-circle";
        switch (status) {
            case ATIVA: return "fa-check-circle";
            case REALOCADA: return "fa-exchange-alt";
            case FINALIZADA: return "fa-check-double";
            case CANCELADA: return "fa-times-circle";
            default: return "fa-question-circle";
        }
    }

    public String getDataAlocacaoFormatada() {
        return dataHoraAlocacao != null ? 
            dataHoraAlocacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
            "N/A";
    }
    
    public String dataAlocacaoFormatada() {
        return getDataAlocacaoFormatada();
    }

    public String getDataFinalizacaoFormatada() {
        return dataHoraFinalizacao != null ? 
            dataHoraFinalizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
            "N/A";
    }
    
    public String dataFinalizacaoFormatada() {
        return getDataFinalizacaoFormatada();
    }

    private String calcularDuracao() {
        if (dataHoraAlocacao == null) return "N/A";
        
        LocalDateTime fim = dataHoraFinalizacao != null ? dataHoraFinalizacao : LocalDateTime.now();
        java.time.Duration duracao = java.time.Duration.between(dataHoraAlocacao, fim);
        
        long dias = duracao.toDays();
        long horas = duracao.toHours() % 24;
        long minutos = duracao.toMinutes() % 60;
        
        if (dias > 0) {
            return String.format("%d dia(s), %d hora(s)", dias, horas);
        } else if (horas > 0) {
            return String.format("%d hora(s), %d min", horas, minutos);
        } else {
            return String.format("%d minuto(s)", minutos);
        }
    }
}