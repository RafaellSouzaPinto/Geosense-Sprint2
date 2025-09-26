package geosense.Geosense.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
public class Patio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(length = 255)
    private String localizacao;

    @Column(name = "endereco_detalhado", length = 255)
    private String enderecoDetalhado;

    @Column(name = "NOME_UNIDADE", length = 255)
    private String nomeUnidade;

    @Column
    private Integer capacidade;

    @OneToMany(mappedBy = "patio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vaga> vagas;

    public Patio() {
    }

    public Patio(Long id, String localizacao, String enderecoDetalhado, String nomeUnidade, Integer capacidade, List<Vaga> vagas) {
        this.id = id;
        this.localizacao = localizacao;
        this.enderecoDetalhado = enderecoDetalhado;
        this.nomeUnidade = nomeUnidade;
        this.capacidade = capacidade;
        this.vagas = vagas;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getEnderecoDetalhado() {
        return enderecoDetalhado;
    }

    public void setEnderecoDetalhado(String enderecoDetalhado) {
        this.enderecoDetalhado = enderecoDetalhado;
    }

    public String getNomeUnidade() {
        return nomeUnidade;
    }

    public void setNomeUnidade(String nomeUnidade) {
        this.nomeUnidade = nomeUnidade;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public List<Vaga> getVagas() {
        return vagas;
    }

    public void setVagas(List<Vaga> vagas) {
        this.vagas = vagas;
    }
}
