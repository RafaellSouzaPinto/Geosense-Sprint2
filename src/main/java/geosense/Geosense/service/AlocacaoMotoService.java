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

/**
 * Service MELHORADO para AlocaçãoMoto
 * Responsabilidades:
 * - Gerenciar alocação de motos com controle de histórico completo
 * - Implementar re-alocação inteligente sem perder histórico
 * - Controlar status das alocações (ATIVA, REALOCADA, FINALIZADA, CANCELADA)
 * - Manter rastreabilidade completa de todas as operações
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
     * ALOCAR: Colocar uma moto numa vaga com controle inteligente de histórico
     * - Se a moto já está alocada, finaliza a alocação anterior como REALOCADA
     * - Cria nova alocação ATIVA
     * - Mantém histórico completo de todas as movimentações
     */
    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto) {
        return alocar(dto, null); // Chama método principal sem usuário específico
    }
    
    /**
     * ALOCAR com usuário responsável pela operação
     */
    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto, Usuario usuarioResponsavel) {
        System.out.println("=== INICIANDO ALOCAÇÃO INTELIGENTE ===");
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
        
        // 2. Verificar se moto já tem alocação ativa
        Optional<AlocacaoMoto> alocacaoAtiva = alocacaoRepository.findAlocacaoAtivaByMoto(moto);
        
        if (alocacaoAtiva.isPresent()) {
            AlocacaoMoto alocacaoAnterior = alocacaoAtiva.get();
            System.out.println("⚠️ Moto " + moto.getModelo() + " já está alocada. Realizando re-alocação...");
            System.out.println("Alocação anterior: Vaga " + alocacaoAnterior.getVaga().getNumero() + 
                             " do Pátio " + alocacaoAnterior.getVaga().getPatio().getNomeUnidade());
            
            // Verificar se é a mesma vaga (não precisa fazer nada)
            if (alocacaoAnterior.getVaga().getId().equals(vaga.getId())) {
                System.out.println("ℹ️ Moto já está na mesma vaga. Nenhuma alteração necessária.");
                return toDTOCompleto(alocacaoAnterior);
            }
            
            // Finalizar alocação anterior como REALOCADA
            finalizarAlocacao(alocacaoAnterior.getId(), StatusAlocacao.REALOCADA, 
                            "Moto realocada para " + vaga.getPatio().getNomeUnidade() + " - Vaga " + vaga.getNumero(),
                            usuarioResponsavel);
        }
        
        // 3. Validações para nova vaga
        validarNovaVaga(vaga, dto.getPatioId());
        
        // 4. Criar nova alocação ATIVA
        AlocacaoMoto novaAlocacao = new AlocacaoMoto();
        novaAlocacao.setMoto(moto);
        novaAlocacao.setVaga(vaga);
        novaAlocacao.setMecanicoResponsavel(mecanico);
        novaAlocacao.setDataHoraAlocacao(LocalDateTime.now());
        novaAlocacao.setObservacoes(dto.getObservacoes());
        novaAlocacao.setStatus(StatusAlocacao.ATIVA);
        
        // 5. Atualizar relacionamentos das entidades
        vaga.setStatus(StatusVaga.OCUPADA);
        vaga.setMoto(moto);
        moto.setVaga(vaga);
        
        // 6. Salvar tudo
        AlocacaoMoto salva = alocacaoRepository.save(novaAlocacao);
        vagaRepository.save(vaga);
        motoRepository.save(moto);
        
        System.out.println("✅ Alocação criada com sucesso!");
        System.out.println("Nova alocação ID: " + salva.getId());
        
        return toDTOCompleto(salva);
    }
    
    /**
     * DESALOCAR: Finalizar alocação mantendo histórico
     */
    public void desalocar(Long alocacaoId) {
        desalocar(alocacaoId, "Desalocação manual", null);
    }
    
    /**
     * DESALOCAR com motivo e usuário responsável
     */
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
    
    /**
     * CANCELAR alocação (diferente de desalocar)
     */
    public void cancelarAlocacao(Long alocacaoId, String motivo, Usuario usuarioResponsavel) {
        System.out.println("=== CANCELANDO ALOCAÇÃO ===");
        
        finalizarAlocacao(alocacaoId, StatusAlocacao.CANCELADA, motivo, usuarioResponsavel);
        
        System.out.println("✅ Alocação cancelada! Histórico mantido.");
    }
    
    /**
     * Método interno para finalizar alocação com status específico
     */
    private void finalizarAlocacao(Long alocacaoId, StatusAlocacao novoStatus, String motivo, Usuario usuarioResponsavel) {
        AlocacaoMoto alocacao = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new RuntimeException("Alocação não encontrada"));
        
        if (!alocacao.isAtiva()) {
            System.out.println("⚠️ Alocação " + alocacaoId + " já estava finalizada");
            return;
        }
        
        Vaga vaga = alocacao.getVaga();
        Moto moto = alocacao.getMoto();
        
        // Finalizar alocação mantendo histórico
        alocacao.finalizarAlocacao(novoStatus, motivo, usuarioResponsavel);
        
        // Liberar vaga e moto apenas se não for re-alocação
        if (novoStatus != StatusAlocacao.REALOCADA) {
            vaga.setStatus(StatusVaga.DISPONIVEL);
            vaga.setMoto(null);
            moto.setVaga(null);
            
            vagaRepository.save(vaga);
            motoRepository.save(moto);
        }
        
        // Salvar alocação com novo status (não deletar!)
        alocacaoRepository.save(alocacao);
        
        System.out.println("Status da alocação " + alocacaoId + " alterado para: " + novoStatus);
    }
    
    /**
     * LISTAR apenas alocações ativas (atualmente em uso)
     */
    public List<AlocacaoMotoDTO> listarAlocacoesAtivas() {
        return alocacaoRepository.findAlocacoesAtivas().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * LISTAR histórico completo (todas as alocações - ativas e finalizadas)
     */
    public List<AlocacaoMotoDTO> listarHistoricoCompleto() {
        return alocacaoRepository.findAllWithDetails().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * LISTAR apenas alocações finalizadas (histórico)
     */
    public List<AlocacaoMotoDTO> listarHistoricoFinalizadas() {
        return alocacaoRepository.findHistoricoAlocacoes().stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * BUSCAR histórico de uma moto específica
     */
    public List<AlocacaoMotoDTO> buscarHistoricoPorMoto(Long motoId) {
        Moto moto = motoRepository.findById(motoId)
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        return alocacaoRepository.findHistoricoByMoto(moto).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * BUSCAR alocação ativa de uma moto
     */
    public Optional<AlocacaoMotoDTO> buscarAlocacaoAtivaPorMoto(Long motoId) {
        Moto moto = motoRepository.findById(motoId)
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        
        return alocacaoRepository.findAlocacaoAtivaByMoto(moto)
                .map(this::toDTOCompleto);
    }
    
    /**
     * BUSCAR alocações por pátio
     */
    public List<AlocacaoMotoDTO> buscarPorPatio(Long patioId) {
        return alocacaoRepository.findByPatioId(patioId).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * BUSCAR alocações por status
     */
    public List<AlocacaoMotoDTO> buscarPorStatus(StatusAlocacao status) {
        return alocacaoRepository.findByStatus(status).stream()
                .map(this::toDTOCompleto)
                .collect(Collectors.toList());
    }
    
    /**
     * BUSCAR alocação por ID
     */
    public Optional<AlocacaoMotoDTO> buscarPorId(Long id) {
        return alocacaoRepository.findById(id).map(this::toDTOCompleto);
    }
    
    /**
     * ESTATÍSTICAS: Contadores úteis
     */
    public AlocacaoEstatisticas obterEstatisticas() {
        long ativas = alocacaoRepository.countAlocacoesAtivas();
        long totalAlocacoes = alocacaoRepository.count();
        long finalizadas = totalAlocacoes - ativas;
        
        return new AlocacaoEstatisticas(ativas, finalizadas, totalAlocacoes);
    }
    
    /**
     * Verificar se uma moto tem alocação ativa
     */
    public boolean motoTemAlocacaoAtiva(Long motoId) {
        return alocacaoRepository.existsAlocacaoAtivaByMotoId(motoId);
    }
    
    /**
     * VALIDAÇÕES para nova vaga (não verifica re-alocação, isso já foi tratado)
     */
    private void validarNovaVaga(Vaga vaga, Long patioId) {
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
     * Converter entidade para DTO básico (compatibilidade)
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
    
    /**
     * Converter entidade para DTO completo com todas as informações
     */
    private AlocacaoMotoDTO toDTOCompleto(AlocacaoMoto alocacao) {
        AlocacaoMotoDTO dto = new AlocacaoMotoDTO();
        
        // IDs básicos
        dto.setId(alocacao.getId());
        dto.setMotoId(alocacao.getMoto().getId());
        dto.setPatioId(alocacao.getVaga().getPatio().getId());
        dto.setVagaId(alocacao.getVaga().getId());
        dto.setMecanicoId(alocacao.getMecanicoResponsavel() != null ? alocacao.getMecanicoResponsavel().getId() : null);
        
        // Campos de controle
        dto.setDataHoraAlocacao(alocacao.getDataHoraAlocacao());
        dto.setDataHoraFinalizacao(alocacao.getDataHoraFinalizacao());
        dto.setStatus(alocacao.getStatus());
        dto.setObservacoes(alocacao.getObservacoes());
        dto.setMotivoFinalizacao(alocacao.getMotivoFinalizacao());
        dto.setUsuarioFinalizacaoId(alocacao.getUsuarioFinalizacao() != null ? alocacao.getUsuarioFinalizacao().getId() : null);
        
        // Informações formatadas para exibição
        dto.setMotoInfo(formatarMotoInfo(alocacao.getMoto()));
        dto.setPatioInfo(alocacao.getVaga().getPatio().getNomeUnidade());
        dto.setVagaInfo("Vaga " + alocacao.getVaga().getNumero());
        dto.setMecanicoInfo(alocacao.getMecanicoResponsavel() != null ? alocacao.getMecanicoResponsavel().getNome() : "N/A");
        dto.setUsuarioFinalizacaoInfo(alocacao.getUsuarioFinalizacao() != null ? alocacao.getUsuarioFinalizacao().getNome() : "N/A");
        
        return dto;
    }
    
    /**
     * Formatar informações da moto para exibição
     */
    private String formatarMotoInfo(Moto moto) {
        String identificacao = moto.getPlaca() != null && !moto.getPlaca().isEmpty() ? 
                             moto.getPlaca() : moto.getChassi();
        return moto.getModelo() + " - " + identificacao;
    }
    
    /**
     * Classe para estatísticas de alocação
     */
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
    
    /**
     * Método auxiliar para compatibilidade - lista todas as alocações (antigo comportamento)
     */
    public List<AlocacaoMotoDTO> listarTodas() {
        return listarHistoricoCompleto();
    }
}