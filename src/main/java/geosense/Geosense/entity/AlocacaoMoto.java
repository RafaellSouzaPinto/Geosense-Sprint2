package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;


@Entity
@Table(name = "ALOCACAO_MOTO")
public class AlocacaoMoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "MOTO_ID")
    @NotNull
    private Moto moto;

    @ManyToOne
    @JoinColumn(name = "VAGA_ID")
    @NotNull
    private Vaga vaga;

    @ManyToOne
    @JoinColumn(name = "MECANICO_RESPONSAVEL_ID")
    private Usuario mecanicoResponsavel;

    @Column(name = "DATA_HORA_ALOCACAO")
    private LocalDateTime dataHoraAlocacao;

    @Column(name = "DATA_HORA_FINALIZACAO")
    private LocalDateTime dataHoraFinalizacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private StatusAlocacao status = StatusAlocacao.ATIVA;

    @Column(name = "MOTIVO_FINALIZACAO", length = 500)
    private String motivoFinalizacao;

    @ManyToOne
    @JoinColumn(name = "USUARIO_FINALIZACAO_ID")
    private Usuario usuarioFinalizacao;

    @Column(name = "OBSERVACOES", length = 1000)
    private String observacoes;

    public enum StatusAlocacao {
        ATIVA,
        REALOCADA,
        FINALIZADA,
        CANCELADA
    }

    public AlocacaoMoto() {
    }

    public AlocacaoMoto(Long id, Moto moto, Vaga vaga, Usuario mecanicoResponsavel, LocalDateTime dataHoraAlocacao, String observacoes) {
        this.id = id;
        this.moto = moto;
        this.vaga = vaga;
        this.mecanicoResponsavel = mecanicoResponsavel;
        this.dataHoraAlocacao = dataHoraAlocacao;
        this.observacoes = observacoes;
        this.status = StatusAlocacao.ATIVA;
    }

    public AlocacaoMoto(Long id, Moto moto, Vaga vaga, Usuario mecanicoResponsavel,
                       LocalDateTime dataHoraAlocacao, LocalDateTime dataHoraFinalizacao,
                       StatusAlocacao status, String motivoFinalizacao, Usuario usuarioFinalizacao,
                       String observacoes) {
        this.id = id;
        this.moto = moto;
        this.vaga = vaga;
        this.mecanicoResponsavel = mecanicoResponsavel;
        this.dataHoraAlocacao = dataHoraAlocacao;
        this.dataHoraFinalizacao = dataHoraFinalizacao;
        this.status = status;
        this.motivoFinalizacao = motivoFinalizacao;
        this.usuarioFinalizacao = usuarioFinalizacao;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Moto getMoto() {
        return moto;
    }

    public void setMoto(Moto moto) {
        this.moto = moto;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public Usuario getMecanicoResponsavel() {
        return mecanicoResponsavel;
    }

    public void setMecanicoResponsavel(Usuario mecanicoResponsavel) {
        this.mecanicoResponsavel = mecanicoResponsavel;
    }

    public LocalDateTime getDataHoraAlocacao() {
        return dataHoraAlocacao;
    }

    public void setDataHoraAlocacao(LocalDateTime dataHoraAlocacao) {
        this.dataHoraAlocacao = dataHoraAlocacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataHoraFinalizacao() {
        return dataHoraFinalizacao;
    }

    public void setDataHoraFinalizacao(LocalDateTime dataHoraFinalizacao) {
        this.dataHoraFinalizacao = dataHoraFinalizacao;
    }

    public StatusAlocacao getStatus() {
        return status;
    }

    public void setStatus(StatusAlocacao status) {
        this.status = status;
    }

    public String getMotivoFinalizacao() {
        return motivoFinalizacao;
    }

    public void setMotivoFinalizacao(String motivoFinalizacao) {
        this.motivoFinalizacao = motivoFinalizacao;
    }

    public Usuario getUsuarioFinalizacao() {
        return usuarioFinalizacao;
    }

    public void setUsuarioFinalizacao(Usuario usuarioFinalizacao) {
        this.usuarioFinalizacao = usuarioFinalizacao;
    }

    public boolean isAtiva() {
        return status == StatusAlocacao.ATIVA;
    }

    public boolean isRealocada() {
        return status == StatusAlocacao.REALOCADA;
    }

    public boolean isFinalizada() {
        return status == StatusAlocacao.FINALIZADA || status == StatusAlocacao.CANCELADA || status == StatusAlocacao.REALOCADA;
    }

    public void finalizarAlocacao(StatusAlocacao novoStatus, String motivo, Usuario usuario) {
        this.status = novoStatus;
        this.dataHoraFinalizacao = LocalDateTime.now();
        this.motivoFinalizacao = motivo;
        this.usuarioFinalizacao = usuario;
    }

    public String statusFormatado() {
        if (status == null) return "Ativa";
        
        switch (status) {
            case ATIVA: return "Ativa";
            case REALOCADA: return "Realocada";
            case FINALIZADA: return "Finalizada";
            case CANCELADA: return "Cancelada";
            default: return "Ativa";
        }
    }

    public String getIconeStatus() {
        if (status == null) return "fa-check-circle";
        
        switch (status) {
            case ATIVA: return "fa-check-circle";
            case REALOCADA: return "fa-arrow-right";
            case FINALIZADA: return "fa-check";
            case CANCELADA: return "fa-times";
            default: return "fa-check-circle";
        }
    }
}
