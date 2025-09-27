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
    private Vaga vaga   ;

    @ManyToOne
    @JoinColumn(name = "MECANICO_RESPONSAVEL_ID")
    private Usuario mecanicoResponsavel;

    @Column(name = "DATA_HORA_ALOCACAO")
    private LocalDateTime dataHoraAlocacao;

    @Column(name = "OBSERVACOES", length = 1000)
    private String observacoes;

    public AlocacaoMoto() {
    }

    public AlocacaoMoto(Long id, Moto moto, Vaga vaga, Usuario mecanicoResponsavel, LocalDateTime dataHoraAlocacao, String observacoes) {
        this.id = id;
        this.moto = moto;
        this.vaga = vaga;
        this.mecanicoResponsavel = mecanicoResponsavel;
        this.dataHoraAlocacao = dataHoraAlocacao;
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
}
