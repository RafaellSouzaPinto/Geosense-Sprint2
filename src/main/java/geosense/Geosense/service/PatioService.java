package geosense.Geosense.service;

import geosense.Geosense.dto.PatioDTO;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.Patio;
import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.PatioRepository;
import geosense.Geosense.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatioService {

    @Autowired
    private PatioRepository patioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private MotoRepository motoRepository;

    public PatioDTO criarPatio(PatioDTO dto) {
        Patio patio = new Patio();
        patio.setLocalizacao(dto.getLocalizacao());
        patio.setEnderecoDetalhado(dto.getEnderecoDetalhado());
        patio.setNomeUnidade(dto.getNomeUnidade());
        patio.setCapacidade(dto.getCapacidade());

        Patio salvo = patioRepository.save(patio);
        
        if (dto.getCapacidade() != null && dto.getCapacidade() > 0) {
            List<Vaga> vagas = new ArrayList<>();
            for (int i = 1; i <= dto.getCapacidade(); i++) {
                Vaga vaga = new Vaga();
                vaga.setNumero(i);
                vaga.setStatus(StatusVaga.DISPONIVEL);
                vaga.setPatio(salvo);
                vagas.add(vaga);
            }
            List<Vaga> vagasSalvas = vagaRepository.saveAll(vagas);
            salvo.setVagas(vagasSalvas);
        }

        return toDTO(salvo);
    }

    public Optional<PatioDTO> buscarPorId(Long id) {
        return patioRepository.findById(id).map(this::toDTO);
    }

    public List<PatioDTO> listarTodos() {
        return patioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PatioDTO atualizar(Long id, PatioDTO dto) {
        Patio patio = patioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pátio não encontrado"));
        
        patio.setLocalizacao(dto.getLocalizacao());
        patio.setEnderecoDetalhado(dto.getEnderecoDetalhado());
        patio.setNomeUnidade(dto.getNomeUnidade());
        
        if (dto.getCapacidade() != null && !dto.getCapacidade().equals(patio.getCapacidade())) {
            ajustarCapacidade(patio, dto.getCapacidade());
        }
        
        patio.setCapacidade(dto.getCapacidade());
        Patio salvo = patioRepository.save(patio);
        
        return toDTO(salvo);
    }

    private void ajustarCapacidade(Patio patio, Integer novaCapacidade) {
        long vagasExistentes = vagaRepository.countByPatioId(patio.getId());
        
        if (novaCapacidade > vagasExistentes) {
            List<Vaga> novasVagas = new ArrayList<>();
            for (int i = (int) vagasExistentes + 1; i <= novaCapacidade; i++) {
                Vaga vaga = new Vaga();
                vaga.setNumero(i);
                vaga.setStatus(StatusVaga.DISPONIVEL);
                vaga.setPatio(patio);
                novasVagas.add(vaga);
            }
            vagaRepository.saveAll(novasVagas);
        } else if (novaCapacidade < vagasExistentes) {
            List<Vaga> todasVagas = vagaRepository.findByPatioIdOrderByNumeroAsc(patio.getId());
            List<Vaga> vagasParaRemover = todasVagas.stream()
                    .filter(v -> v.getNumero() > novaCapacidade)
                    .collect(Collectors.toList());
            
            boolean temVagaOcupada = vagasParaRemover.stream()
                    .anyMatch(v -> v.getStatus() == StatusVaga.OCUPADA);
            
            if (temVagaOcupada) {
                throw new RuntimeException("Não é possível reduzir a capacidade. Há vagas ocupadas que seriam removidas.");
            }
            
            vagaRepository.deleteAll(vagasParaRemover);
        }
    }

    public void deletar(Long id) {
        // Verifica se o pátio existe
        if (!patioRepository.existsById(id)) {
            throw new RuntimeException("Pátio não encontrado");
        }
        
        // Buscar todas as vagas do pátio
        List<Vaga> vagasDoPatio = vagaRepository.findByPatioIdOrderByNumeroAsc(id);
        
        // Liberar todas as motos que estão alocadas nas vagas deste pátio
        for (Vaga vaga : vagasDoPatio) {
            if (vaga.getMoto() != null) {
                Moto moto = vaga.getMoto();
                moto.setVaga(null); // Remove a vaga da moto (volta para "sem pátio")
                motoRepository.save(moto);
                
                // Limpa a relação na vaga também
                vaga.setMoto(null);
                vaga.setStatus(StatusVaga.DISPONIVEL);
            }
        }
        
        // Salva as vagas com as relações limpas
        vagaRepository.saveAll(vagasDoPatio);
        
        // Agora pode deletar o pátio (as vagas serão deletadas em cascata devido ao orphanRemoval = true)
        patioRepository.deleteById(id);
    }

    private PatioDTO toDTO(Patio patio) {
        List<Long> vagaIds = patio.getVagas() != null ? 
                patio.getVagas().stream().map(Vaga::getId).collect(Collectors.toList()) : 
                new ArrayList<>();
        
        long vagasOcupadas = vagaRepository.countByPatioIdAndStatus(patio.getId(), StatusVaga.OCUPADA);
        long vagasDisponiveis = vagaRepository.countByPatioIdAndStatus(patio.getId(), StatusVaga.DISPONIVEL);
        long totalVagas = vagaRepository.countByPatioId(patio.getId());
        
        System.out.println("=== CONTADORES PÁTIO " + patio.getId() + " ===");
        System.out.println("Total: " + totalVagas + ", Ocupadas: " + vagasOcupadas + ", Disponíveis: " + vagasDisponiveis);
        
        return new PatioDTO(
                patio.getId(),
                patio.getLocalizacao(),
                patio.getEnderecoDetalhado(),
                patio.getNomeUnidade(),
                patio.getCapacidade(),
                vagaIds,
                (int) vagasOcupadas,
                (int) vagasDisponiveis
        );
    }
}