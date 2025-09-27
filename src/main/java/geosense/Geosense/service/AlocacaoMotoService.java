package geosense.Geosense.service;

import geosense.Geosense.dto.AlocacaoMotoDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.AlocacaoMoto.StatusAlocacao;
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

    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto) {
        return alocar(dto, null); // Chama método principal sem usuário específico
    }

    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto, Usuario usuarioResponsavel) {
        System.out.println("=== INICIANDO ALOCAÇÃO INTELIGENTE ===");
        System.out.println("Moto ID: " + dto.getMotoId());
        System.out.println("Pátio ID: " + dto.getPatioId());
        System.out.println("Vaga ID: " + dto.getVagaId());
        
        Moto moto = motoRepository.findById(dto.getMotoId())
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        Vaga vaga = vagaRepository.findById(dto.getVagaId())
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));
        
        Usuario mecanico = null;
        if (dto.getMecanicoId() != null) {
            mecanico = usuarioRepository.findById(dto.getMecanicoId())
                    .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));
        }
        
        Optional<AlocacaoMoto> alocacaoAtiva = alocacaoRepository.findAlocacaoAtivaByMoto(moto);
        
        if (alocacaoAtiva.isPresent()) {
            AlocacaoMoto alocacaoAnterior = alocacaoAtiva.get();
            System.out.println("Moto " + moto.getModelo() + " já está alocada. Realizando re-alocação...");
            System.out.println("Alocação anterior: Vaga " + alocacaoAnterior.getVaga().getNumero() + 
                             " do Pátio " + alocacaoAnterior.getVaga().getPatio().getNomeUnidade());
            
            if (alocacaoAnterior.getVaga().getId().equals(vaga.getId())) {
                System.out.println("ℹ️ Moto já está na mesma vaga. Nenhuma alteração necessária.");
                return toDTOCompleto(alocacaoAnterior);
            }
            
            finalizarAlocacao(alocacaoAnterior.getId(), StatusAlocacao.REALOCADA,
                            "Moto realocada para " + vaga.getPatio().getNomeUnidade() + " - Vaga " + vaga.getNumero(),
                            usuarioResponsavel);
        }
        
        validarNovaVaga(vaga, dto.getPatioId());
        
        AlocacaoMoto novaAlocacao = new AlocacaoMoto();
        novaAlocacao.setMoto(moto);
        novaAlocacao.setVaga(vaga);
        novaAlocacao.setMecanicoResponsavel(mecanico);
        novaAlocacao.setDataHoraAlocacao(LocalDateTime.now());
        novaAlocacao.setObservacoes(dto.getObservacoes());
        novaAlocacao.setStatus(StatusAlocacao.ATIVA);
        
        vaga.setStatus(StatusVaga.OCUPADA);
        vaga.setMoto(moto);
        moto.setVaga(vaga);
        
        AlocacaoMoto salva = alocacaoRepository.save(novaAlocacao);
        vagaRepository.save(vaga);
        motoRepository.save(moto);
        
        System.out.println("Alocação criada com sucesso!");
        System.out.println("Nova alocação ID: " + salva.getId());
        
        return toDTOCompleto(salva);
    }

    public void desalocar(Long alocacaoId) {
        desalocar(alocacaoId, "Desalocação manual", null);
    }

    public void desalocar(Long alocacaoId, String motivo, Usuario usuarioResponsavel) {
        System.out.println("=== DESALOCANDO COM HISTÓRICO ===");
        
        AlocacaoMoto alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RuntimeException("Alocação não encontrada"));
        
        if (!alocacao.isAtiva()) {
            throw new RuntimeException("Alocação já foi finalizada anteriormente");
        }
        
        finalizarAlocacao(alocacaoId, StatusAlocacao.FINALIZADA, motivo, usuarioResponsavel);
        
        System.out.println("✅ Alocação finalizada com sucesso! Histórico mantido.");
    }

    public void cancelarAlocacao(Long alocacaoId, String motivo, Usuario usuarioResponsavel) {
        System.out.println("=== CANCELANDO ALOCAÇÃO ===");
        
        finalizarAlocacao(alocacaoId, StatusAlocacao.CANCELADA, motivo, usuarioResponsavel);
        
        System.out.println("Alocação cancelada! Histórico mantido.");
    }

    private void finalizarAlocacao(Long alocacaoId, StatusAlocacao novoStatus, String motivo, Usuario usuarioResponsavel) {
        AlocacaoMoto alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RuntimeException("Alocação não encontrada"));
        
        if (!alocacao.isAtiva()) {
            System.out.println("Alocação " + alocacaoId + " já estava finalizada");
            return;
        }
        
        Vaga vaga = alocacao.getVaga();
        Moto moto = alocacao.getMoto();
        
        alocacao.finalizarAlocacao(novoStatus, motivo, usuarioResponsavel);
        
        if (novoStatus != StatusAlocacao.REALOCADA) {
            vaga.setStatus(StatusVaga.DISPONIVEL);
            vaga.setMoto(null);
            moto.setVaga(null);
            
            vagaRepository.save(vaga);
            motoRepository.save(moto);
        }
        
        alocacaoRepository.save(alocacao);
        
        System.out.println("Status da alocação " + alocacaoId + " alterado para: " + novoStatus);
    }

    public List<AlocacaoMotoDTO> listarAlocacoesAtivas() {
        return alocacaoRepository.findAlocacoesAtivas().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public List<AlocacaoMotoDTO> listarHistoricoCompleto() {
        return alocacaoRepository.findAllWithDetails().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public List<AlocacaoMotoDTO> listarHistoricoFinalizadas() {
        return alocacaoRepository.findHistoricoAlocacoes().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public List<AlocacaoMotoDTO> buscarHistoricoPorMoto(Long motoId) {
        Moto moto = motoRepository.findById(motoId)
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        return alocacaoRepository.findHistoricoByMoto(moto).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public Optional<AlocacaoMotoDTO> buscarAlocacaoAtivaPorMoto(Long motoId) {
        Moto moto = motoRepository.findById(motoId)
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        return alocacaoRepository.findAlocacaoAtivaByMoto(moto)
                .map(this::toDTOCompleto);
    }

    public List<AlocacaoMotoDTO> buscarPorPatio(Long patioId) {
        return alocacaoRepository.findByPatioId(patioId).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public List<AlocacaoMotoDTO> buscarPorStatus(StatusAlocacao status) {
        return alocacaoRepository.findByStatus(status).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }

    public Optional<AlocacaoMotoDTO> buscarPorId(Long id) {
        return alocacaoRepository.findById(id).map(this::toDTOCompleto);
    }

    public AlocacaoEstatisticas obterEstatisticas() {
        long ativas = alocacaoRepository.countAlocacoesAtivas();
        long totalAlocacoes = alocacaoRepository.count();
        long finalizadas = totalAlocacoes - ativas;
        
        return new AlocacaoEstatisticas(ativas, finalizadas, totalAlocacoes);
    }

    public boolean motoTemAlocacaoAtiva(Long motoId) {
        return alocacaoRepository.existsAlocacaoAtivaByMotoId(motoId);
    }

    private void validarNovaVaga(Vaga vaga, Long patioId) {
        if (vaga.getStatus() != StatusVaga.DISPONIVEL) {
            throw new RuntimeException("Vaga " + vaga.getNumero() + " não está disponível");
        }
        
        if (vaga.getMoto() != null) {
            throw new RuntimeException("Vaga " + vaga.getNumero() + " já tem uma moto");
        }
        
        if (!vaga.getPatio().getId().equals(patioId)) {
            throw new RuntimeException("Vaga não pertence ao pátio selecionado");
        }
    }

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

    private AlocacaoMotoDTO toDTOCompleto(AlocacaoMoto alocacao) {
        AlocacaoMotoDTO dto = new AlocacaoMotoDTO();
        
        dto.setId(alocacao.getId());
        dto.setMotoId(alocacao.getMoto().getId());
        dto.setPatioId(alocacao.getVaga().getPatio().getId());
        dto.setVagaId(alocacao.getVaga().getId());
        dto.setMecanicoId(alocacao.getMecanicoResponsavel() != null ? alocacao.getMecanicoResponsavel().getId() : null);
        
        dto.setDataHoraAlocacao(alocacao.getDataHoraAlocacao());
        dto.setDataHoraFinalizacao(alocacao.getDataHoraFinalizacao());
        dto.setStatus(alocacao.getStatus());
        dto.setObservacoes(alocacao.getObservacoes());
        dto.setMotivoFinalizacao(alocacao.getMotivoFinalizacao());
        dto.setUsuarioFinalizacaoId(alocacao.getUsuarioFinalizacao() != null ? alocacao.getUsuarioFinalizacao().getId() : null);
        
        dto.setMotoInfo(formatarMotoInfo(alocacao.getMoto()));
        dto.setPatioInfo(alocacao.getVaga().getPatio().getNomeUnidade());
        dto.setVagaInfo("Vaga " + alocacao.getVaga().getNumero());
        dto.setMecanicoInfo(alocacao.getMecanicoResponsavel() != null ? alocacao.getMecanicoResponsavel().getNome() : "N/A");
        dto.setUsuarioFinalizacaoInfo(alocacao.getUsuarioFinalizacao() != null ? alocacao.getUsuarioFinalizacao().getNome() : "N/A");
        
        return dto;
    }

    private String formatarMotoInfo(Moto moto) {
        String identificacao = moto.getPlaca() != null && !moto.getPlaca().isEmpty() ? 
                             moto.getPlaca() : moto.getChassi();
        return moto.getModelo() + " - " + identificacao;
    }

    public static class AlocacaoEstatisticas {
        private final long alocacoesAtivas;
        private final long alocacoesFinalizadas;
        private final long totalAlocacoes;
        
        public AlocacaoEstatisticas(long ativas, long finalizadas, long total) {
            this.alocacoesAtivas = ativas;
            this.alocacoesFinalizadas = finalizadas;
            this.totalAlocacoes = total;
        }
        
        public long getAlocacoesAtivas() { return alocacoesAtivas; }
        public long getAlocacoesFinalizadas() { return alocacoesFinalizadas; }
        public long getTotalAlocacoes() { return totalAlocacoes; }
        
        @Override
        public String toString() {
            return String.format("Estatísticas: %d ativas, %d finalizadas, %d total", 
                               alocacoesAtivas, alocacoesFinalizadas, totalAlocacoes);
        }
    }

    public List<AlocacaoMotoDTO> listarTodas() {
        return listarHistoricoCompleto();
    }
}