package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
public class Moto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String modelo;

    @Size(min = 7, max = 10)
    @Column(nullable = true, unique = true)
    private String placa;

    @Size(max = 50)
    @Column(nullable = true, unique = true)
    private String chassi;

    @Column(nullable = true)
    private String problemaIdentificado;

    @OneToOne
    private Vaga vaga;

    @OneToMany(mappedBy = "moto")
    private List<Defeito> defeitos;

    @OneToMany(mappedBy = "moto")
    private List<AlocacaoMoto> historicoAlocacoes;

    public Moto() {}

    public Moto(Long id, String modelo, String placa, String chassi, String problemaIdentificado, Vaga vaga,
                List<Defeito> defeitos, List<AlocacaoMoto> historicoAlocacoes) {
        this.id = id;
        this.modelo = modelo;
        this.placa = placa;
        this.chassi = chassi;
        this.problemaIdentificado = problemaIdentificado;
        this.vaga = vaga;
        this.defeitos = defeitos;
        this.historicoAlocacoes = historicoAlocacoes;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getChassi() {
        return chassi;
    }

    public void setChassi(String chassi) {
        this.chassi = chassi;
    }

    public String getProblemaIdentificado() {
        return problemaIdentificado;
    }

    public void setProblemaIdentificado(String problemaIdentificado) {
        this.problemaIdentificado = problemaIdentificado;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public List<Defeito> getDefeitos() {
        return defeitos;
    }

    public void setDefeitos(List<Defeito> defeitos) {
        this.defeitos = defeitos;
    }

    public List<AlocacaoMoto> getHistoricoAlocacoes() {
        return historicoAlocacoes;
    }

    public void setHistoricoAlocacoes(List<AlocacaoMoto> historicoAlocacoes) {
        this.historicoAlocacoes = historicoAlocacoes;
    }
}