package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class AlocacaoMoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private Moto moto;

    @ManyToOne
    @NotNull
    private Vaga vaga   ;

    @ManyToOne
    private Usuario mecanicoResponsavel;

    private LocalDateTime dataHoraAlocacao;

    public AlocacaoMoto() {
    }

    public AlocacaoMoto(Long id, Moto moto, Vaga vaga, Usuario mecanicoResponsavel, LocalDateTime dataHoraAlocacao) {
        this.id = id;
        this.moto = moto;
        this.vaga = vaga;
        this.mecanicoResponsavel = mecanicoResponsavel;
        this.dataHoraAlocacao = dataHoraAlocacao;
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
}
