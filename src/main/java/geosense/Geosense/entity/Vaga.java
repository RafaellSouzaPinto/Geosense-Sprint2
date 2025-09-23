package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    private int numero;

    @Enumerated(EnumType.STRING)
    private StatusVaga status;

    @Enumerated(EnumType.STRING)
    private TipoVaga tipo;

    @ManyToOne
    @NotNull
    private Patio patio;

    @OneToOne(mappedBy = "vaga")
    private Moto moto;

    public Vaga() {
    }

    public Vaga(Long id, int numero, StatusVaga status, TipoVaga tipo, Patio patio, Moto moto) {
        this.id = id;
        this.numero = numero;
        this.status = status;
        this.tipo = tipo;
        this.patio = patio;
        this.moto = moto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
        this.status = status;
    }

    public TipoVaga getTipo() {
        return tipo;
    }

    public void setTipo(TipoVaga tipo) {
        this.tipo = tipo;
    }

    public Patio getPatio() {
        return patio;
    }

    public void setPatio(Patio patio) {
        this.patio = patio;
    }

    public Moto getMoto() {
        return moto;
    }

    public void setMoto(Moto moto) {
        this.moto = moto;
    }
}