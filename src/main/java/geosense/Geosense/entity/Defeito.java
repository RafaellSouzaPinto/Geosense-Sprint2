package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Defeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TiposDefeitos tiposDefeitos;

    @NotBlank
    @Size(max = 255)
    private String descricao;

    @ManyToOne
    @NotNull
    private Moto moto;

    public Defeito() {
    }

    public Defeito(Long id, TiposDefeitos tiposDefeitos, String descricao, Moto moto) {
        this.id = id;
        this.tiposDefeitos = tiposDefeitos;
        this.descricao = descricao;
        this.moto = moto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TiposDefeitos getTiposDefeitos() {
        return tiposDefeitos;
    }

    public void setTiposDefeitos(TiposDefeitos tiposDefeitos) {
        this.tiposDefeitos = tiposDefeitos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Moto getMoto() {
        return moto;
    }

    public void setMoto(Moto moto) {
        this.moto = moto;
    }
}