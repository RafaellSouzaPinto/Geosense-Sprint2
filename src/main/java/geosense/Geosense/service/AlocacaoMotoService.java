package geosense.Geosense.service;

import geosense.Geosense.dto.AlocacaoMotoDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service SIMPLIFICADO para AlocaçãoMoto
 * Responsabilidade: Gerenciar alocação de motos em vagas de pátios
 */
@Service
@Transactional
public class AlocacaoMotoService {

    @Autowired
    private AlocacaoMotoRepository alocacaoRepository;
    
    @Autowired
    private MotoRepository motoRepository;
    
    @Autowired
    private VagaRepository vagaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * ALOCAR: Colocar uma moto numa vaga
     */
    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto) {
        System.out.println("=== INICIANDO ALOCAÇÃO ===");
        System.out.println("Moto ID: " + dto.getMotoId());
        System.out.println("Pátio ID: " + dto.getPatioId());
        System.out.println("Vaga ID: " + dto.getVagaId());
        
        // 1. Buscar entidades
        Moto moto = motoRepository.findById(dto.getMotoId())
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        Vaga vaga = vagaRepository.findById(dto.getVagaId())
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));
        
        Usuario mecanico = null;
        if (dto.getMecanicoId() != null) {
            mecanico = usuarioRepository.findById(dto.getMecanicoId())
                    .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));
        }
        
        // 2. Validações simples
        validarAlocacao(moto, vaga, dto.getPatioId());
        
        // 3. Criar alocação
        AlocacaoMoto alocacao = new AlocacaoMoto();
        alocacao.setMoto(moto);
        alocacao.setVaga(vaga);
        alocacao.setMecanicoResponsavel(mecanico);
        alocacao.setDataHoraAlocacao(LocalDateTime.now());
        alocacao.setObservacoes(dto.getObservacoes());
        
        // 4. Atualizar relacionamentos
        vaga.setStatus(StatusVaga.OCUPADA);
        vaga.setMoto(moto);
        moto.setVaga(vaga);
        
        // 5. Salvar tudo
        AlocacaoMoto salva = alocacaoRepository.save(alocacao);
        vagaRepository.save(vaga);
        motoRepository.save(moto);
        
        System.out.println("✅ Alocação criada com sucesso!");
        
        return toDTO(salva);
    }
    
    /**
     * DESALOCAR: Remover moto da vaga
     */
    public void desalocar(Long alocacaoId) {
        System.out.println("=== DESALOCANDO ===");
        
        AlocacaoMoto alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RuntimeException("Alocação não encontrada"));
        
        Vaga vaga = alocacao.getVaga();
        Moto moto = alocacao.getMoto();
        
        // Liberar vaga
        vaga.setStatus(StatusVaga.DISPONIVEL);
        vaga.setMoto(null);
        moto.setVaga(null);
        
        // Salvar e deletar
        vagaRepository.save(vaga);
        motoRepository.save(moto);
        alocacaoRepository.delete(alocacao);
        
        System.out.println("✅ Alocação removida com sucesso!");
    }
    
    /**
     * LISTAR todas as alocações
     */
    public List<AlocacaoMotoDTO> listarTodas() {
        return alocacaoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * BUSCAR alocação por ID
     */
    public Optional<AlocacaoMotoDTO> buscarPorId(Long id) {
        return alocacaoRepository.findById(id).map(this::toDTO);
    }
    
    /**
     * VALIDAÇÕES antes de alocar
     */
    private void validarAlocacao(Moto moto, Vaga vaga, Long patioId) {
        // Se moto já está alocada, desalocar primeiro (re-alocação)
        if (moto.getVaga() != null) {
            System.out.println("⚠️ Moto " + moto.getModelo() + " já está alocada na vaga " + moto.getVaga().getNumero() + ". Fazendo re-alocação...");
            
            Vaga vagaAnterior = moto.getVaga();
            vagaAnterior.setStatus(StatusVaga.DISPONIVEL);
            vagaAnterior.setMoto(null);
            moto.setVaga(null);
            
            vagaRepository.save(vagaAnterior);
            motoRepository.save(moto);
            
            System.out.println("✅ Vaga " + vagaAnterior.getNumero() + " liberada para re-alocação");
        }
        
        // Vaga está disponível?
        if (vaga.getStatus() != StatusVaga.DISPONIVEL) {
            throw new RuntimeException("Vaga " + vaga.getNumero() + " não está disponível");
        }
        
        // Vaga já tem moto?
        if (vaga.getMoto() != null) {
            throw new RuntimeException("Vaga " + vaga.getNumero() + " já tem uma moto");
        }
        
        // Vaga pertence ao pátio selecionado?
        if (!vaga.getPatio().getId().equals(patioId)) {
            throw new RuntimeException("Vaga não pertence ao pátio selecionado");
        }
    }
    
    /**
     * Converter entidade para DTO
     */
    private AlocacaoMotoDTO toDTO(AlocacaoMoto alocacao) {
        return new AlocacaoMotoDTO(
                alocacao.getId(),
                alocacao.getMoto().getId(),
                alocacao.getVaga().getPatio().getId(),
                alocacao.getVaga().getId(),
                alocacao.getMecanicoResponsavel() != null ? alocacao.getMecanicoResponsavel().getId() : null,
                alocacao.getObservacoes()
        );
    }
}