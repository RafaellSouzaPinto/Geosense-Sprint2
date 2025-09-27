package geosense.Geosense.controller;

import geosense.Geosense.dto.AlocacaoMotoDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.service.AlocacaoMotoService;
import geosense.Geosense.service.PatioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller MELHORADO para AlocaçãoMoto
 * Recursos:
 * - Visualização separada: alocações ativas vs histórico completo
 * - Controle inteligente de re-alocações
 * - Rastreamento completo de status e histórico
 * - Estatísticas de uso
 */
@Controller
@RequestMapping("/alocacoes")
public class AlocacaoMotoController {

    @Autowired
    private AlocacaoMotoService alocacaoService;
    
    @Autowired
    private AlocacaoMotoRepository alocacaoRepository;
    
    @Autowired
    private MotoRepository motoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PatioService patioService;

    /**
     * PÁGINA PRINCIPAL: Alocações ativas (default)
     */
    @GetMapping
    public String listar(Model model) {
        return listarAtivas(model);
    }
    
    /**
     * LISTAR apenas alocações ativas (em uso no momento)
     */
    @GetMapping("/ativas")
    public String listarAtivas(Model model) {
        List<AlocacaoMotoDTO> alocacoesAtivas = alocacaoService.listarAlocacoesAtivas();
        AlocacaoMotoService.AlocacaoEstatisticas stats = alocacaoService.obterEstatisticas();
        
        System.out.println("=== LISTANDO ALOCAÇÕES ATIVAS ===");
        System.out.println("Alocações ativas encontradas: " + alocacoesAtivas.size());
        System.out.println(stats.toString());
        
        alocacoesAtivas.forEach(a -> {
            System.out.println("- Alocação " + a.getId() + ": " + a.getMotoInfo() + 
                             " na " + a.getVagaInfo() + " do " + a.getPatioInfo() +
                             " desde " + a.getDataAlocacaoFormatada());
        });
        
        model.addAttribute("alocacoes", alocacoesAtivas);
        model.addAttribute("stats", stats);
        model.addAttribute("tipoLista", "ativas");
        model.addAttribute("tituloLista", "Alocações Ativas");
        return "alocacoes/list";
    }
    
    /**
     * HISTÓRICO completo (todas as alocações - ativas e finalizadas)
     */
    @GetMapping("/historico")
    public String listarHistorico(Model model) {
        List<AlocacaoMotoDTO> historicoCompleto = alocacaoService.listarHistoricoCompleto();
        AlocacaoMotoService.AlocacaoEstatisticas stats = alocacaoService.obterEstatisticas();
        
        System.out.println("=== LISTANDO HISTÓRICO COMPLETO ===");
        System.out.println("Total de alocações no histórico: " + historicoCompleto.size());
        System.out.println(stats.toString());
        
        model.addAttribute("alocacoes", historicoCompleto);
        model.addAttribute("stats", stats);
        model.addAttribute("tipoLista", "historico");
        model.addAttribute("tituloLista", "Histórico Completo de Alocações");
        return "alocacoes/list";
    }
    
    /**
     * HISTÓRICO apenas de alocações finalizadas
     */
    @GetMapping("/historico/finalizadas")
    public String listarHistoricoFinalizadas(Model model) {
        List<AlocacaoMotoDTO> historicoFinalizadas = alocacaoService.listarHistoricoFinalizadas();
        AlocacaoMotoService.AlocacaoEstatisticas stats = alocacaoService.obterEstatisticas();
        
        System.out.println("=== LISTANDO HISTÓRICO DE FINALIZADAS ===");
        System.out.println("Alocações finalizadas encontradas: " + historicoFinalizadas.size());
        
        model.addAttribute("alocacoes", historicoFinalizadas);
        model.addAttribute("stats", stats);
        model.addAttribute("tipoLista", "finalizadas");
        model.addAttribute("tituloLista", "Histórico de Alocações Finalizadas");
        return "alocacoes/list";
    }

    /**
     * FORMULÁRIO para nova alocação
     */
    @GetMapping("/novo")
    public String novaAlocacao(Model model) {
        // Buscar TODAS as motos do sistema (não só as sem vaga)
        List<Moto> todasMotos = motoRepository.findAll();
        List<Usuario> mecanicos = usuarioRepository.findByTipo(TipoUsuario.MECANICO);
        
        model.addAttribute("alocacao", new AlocacaoMotoDTO());
        model.addAttribute("motos", todasMotos);
        model.addAttribute("patios", patioService.listarTodos());
        model.addAttribute("mecanicos", mecanicos);
        
        System.out.println("=== FORMULÁRIO NOVA ALOCAÇÃO ===");
        System.out.println("Total de motos no sistema: " + todasMotos.size());
        System.out.println("Motos sem vaga: " + motoRepository.findMotosSemVaga().size());
        System.out.println("Motos com vaga: " + motoRepository.findMotosComVaga().size());
        System.out.println("Pátios: " + patioService.listarTodos().size());
        
        // Debug: listar todas as motos
        if (todasMotos.isEmpty()) {
            System.out.println("❌ PROBLEMA: Não há motos cadastradas no sistema!");
            System.out.println("💡 SOLUÇÃO: Cadastre uma nova moto primeiro.");
        } else {
            System.out.println("✅ Todas as motos do sistema:");
            todasMotos.forEach(m -> {
                String status = m.getVaga() != null ? "ALOCADA (Vaga " + m.getVaga().getNumero() + ")" : "LIVRE";
                System.out.println("- Moto " + m.getId() + ": " + m.getModelo() + " (" + (m.getPlaca() != null ? m.getPlaca() : m.getChassi()) + ") - " + status);
            });
        }
        
        return "alocacoes/form";
    }

    /**
     * CRIAR nova alocação
     */
    @PostMapping
    public String criar(@Valid @ModelAttribute("alocacao") AlocacaoMotoDTO dto,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        System.out.println("=== CRIANDO ALOCAÇÃO ===");
        System.out.println("DTO recebido: " + dto.getMotoId() + ", " + dto.getPatioId() + ", " + dto.getVagaId());
        
        if (result.hasErrors()) {
            System.out.println("Erros de validação:");
            result.getAllErrors().forEach(error -> System.out.println("- " + error.getDefaultMessage()));
            
            // Recarregar dados para o formulário
            model.addAttribute("alocacao", dto);  // ✅ Adicionar o objeto alocacao para o Thymeleaf
            carregarDadosFormulario(model);
            return "alocacoes/form";
        }
        
        try {
            alocacaoService.alocar(dto);
            redirectAttributes.addFlashAttribute("success", "Moto alocada com sucesso!");
            return "redirect:/alocacoes";
            
        } catch (Exception e) {
            System.err.println("Erro ao alocar: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("alocacao", dto);  // ✅ Adicionar o objeto alocacao para o Thymeleaf
            carregarDadosFormulario(model);
            return "alocacoes/form";
        }
    }

    /**
     * HISTÓRICO de uma moto específica
     */
    @GetMapping("/moto/{motoId}/historico")
    public String listarHistoricoPorMoto(@PathVariable Long motoId, Model model) {
        try {
            List<AlocacaoMotoDTO> historico = alocacaoService.buscarHistoricoPorMoto(motoId);
            
            System.out.println("=== HISTÓRICO DA MOTO " + motoId + " ===");
            System.out.println("Total de alocações encontradas: " + historico.size());
            
            model.addAttribute("alocacoes", historico);
            model.addAttribute("motoId", motoId);
            model.addAttribute("tipoLista", "historico-moto");
            model.addAttribute("tituloLista", "Histórico da Moto");
            return "alocacoes/list";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao buscar histórico da moto: " + e.getMessage());
            return "redirect:/alocacoes";
        }
    }
    
    /**
     * ALOCAÇÕES de um pátio específico
     */
    @GetMapping("/patio/{patioId}")
    public String listarPorPatio(@PathVariable Long patioId, Model model) {
        try {
            List<AlocacaoMotoDTO> alocacoesPatio = alocacaoService.buscarPorPatio(patioId);
            AlocacaoMotoService.AlocacaoEstatisticas stats = alocacaoService.obterEstatisticas();
            
            System.out.println("=== ALOCAÇÕES DO PÁTIO " + patioId + " ===");
            System.out.println("Total de alocações encontradas: " + alocacoesPatio.size());
            
            model.addAttribute("alocacoes", alocacoesPatio);
            model.addAttribute("stats", stats);
            model.addAttribute("patioId", patioId);
            model.addAttribute("tipoLista", "patio");
            model.addAttribute("tituloLista", "Alocações do Pátio");
            return "alocacoes/list";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao buscar alocações do pátio: " + e.getMessage());
            return "redirect:/alocacoes";
        }
    }
    
    /**
     * REMOVER alocação (desalocar moto) - mantém histórico
     */
    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            alocacaoService.desalocar(id, "Desalocação via interface web", null);
            redirectAttributes.addFlashAttribute("success", "Moto desalocada com sucesso! Histórico mantido.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao desalocar: " + e.getMessage());
        }
        return "redirect:/alocacoes";
    }
    
    /**
     * CANCELAR alocação - diferente de remover
     */
    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, 
                          @RequestParam(required = false) String motivo,
                          RedirectAttributes redirectAttributes) {
        try {
            String motivoCancelamento = motivo != null && !motivo.trim().isEmpty() ? 
                                      motivo : "Cancelamento via interface web";
            alocacaoService.cancelarAlocacao(id, motivoCancelamento, null);
            redirectAttributes.addFlashAttribute("success", "Alocação cancelada com sucesso! Histórico mantido.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar: " + e.getMessage());
        }
        return "redirect:/alocacoes";
    }
    
    /**
     * Helper: Carregar dados para o formulário
     */
    private void carregarDadosFormulario(Model model) {
        List<Moto> todasMotos = motoRepository.findAll();
        List<Usuario> mecanicos = usuarioRepository.findByTipo(TipoUsuario.MECANICO);
        
        model.addAttribute("motos", todasMotos);
        model.addAttribute("patios", patioService.listarTodos());
        model.addAttribute("mecanicos", mecanicos);
        
        System.out.println("Dados recarregados - Motos: " + todasMotos.size() + ", Pátios: " + patioService.listarTodos().size());
    }
    
    /**
     * PÁGINA DE DEBUG
     */
    @GetMapping("/debug")
    public String paginaDebug() {
        return "debug";
    }
    
    /**
     * ESTATÍSTICAS das alocações
     */
    @GetMapping("/estatisticas")
    @ResponseBody
    public AlocacaoMotoService.AlocacaoEstatisticas obterEstatisticas() {
        return alocacaoService.obterEstatisticas();
    }
    
    /**
     * API: Verificar se moto tem alocação ativa
     */
    @GetMapping("/api/moto/{motoId}/ativa")
    @ResponseBody
    public boolean motoTemAlocacaoAtiva(@PathVariable Long motoId) {
        return alocacaoService.motoTemAlocacaoAtiva(motoId);
    }
    
    /**
     * API: Buscar alocação ativa de uma moto
     */
    @GetMapping("/api/moto/{motoId}/alocacao-ativa")
    @ResponseBody
    public Optional<AlocacaoMotoDTO> buscarAlocacaoAtivaPorMoto(@PathVariable Long motoId) {
        return alocacaoService.buscarAlocacaoAtivaPorMoto(motoId);
    }
    
    /**
     * ENDPOINT DE DEBUG: Finalizar todas as alocações ativas (mantém histórico)
     */
    @PostMapping("/debug/finalizar-todas")
    @ResponseBody
    public String finalizarTodasAsAlocacoes() {
        try {
            List<AlocacaoMotoDTO> alocacoesAtivas = alocacaoService.listarAlocacoesAtivas();
            
            System.out.println("=== FINALIZANDO TODAS AS ALOCAÇÕES ATIVAS ===");
            System.out.println("Total de alocações ativas encontradas: " + alocacoesAtivas.size());
            
            if (alocacoesAtivas.isEmpty()) {
                return "❌ Nenhuma alocação ativa encontrada para finalizar.";
            }
            
            for (AlocacaoMotoDTO alocacao : alocacoesAtivas) {
                System.out.println("Finalizando: Alocação " + alocacao.getId() + 
                                 " - Moto " + alocacao.getMotoId() + " da Vaga " + alocacao.getVagaId());
                alocacaoService.desalocar(alocacao.getId(), "Finalização em lote via debug", null);
            }
            
            System.out.println("✅ Todas as alocações ativas foram finalizadas!");
            return "✅ SUCESSO! " + alocacoesAtivas.size() + " alocações foram finalizadas. " +
                   "Histórico mantido. Agora você pode fazer novas alocações.";
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao finalizar alocações: " + e.getMessage());
            e.printStackTrace();
            return "❌ ERRO: " + e.getMessage();
        }
    }
    
    /**
     * ENDPOINT DE DEBUG: Exibir estatísticas completas
     */
    @GetMapping("/debug/stats")
    @ResponseBody
    public String exibirEstatisticas() {
        try {
            AlocacaoMotoService.AlocacaoEstatisticas stats = alocacaoService.obterEstatisticas();
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 ESTATÍSTICAS DE ALOCAÇÕES\n");
            sb.append("============================\n");
            sb.append("🟢 Alocações Ativas: ").append(stats.getAlocacoesAtivas()).append("\n");
            sb.append("🔴 Alocações Finalizadas: ").append(stats.getAlocacoesFinalizadas()).append("\n");
            sb.append("📈 Total de Alocações: ").append(stats.getTotalAlocacoes()).append("\n");
            sb.append("============================\n");
            
            if (stats.getTotalAlocacoes() > 0) {
                double percentualAtivas = (double) stats.getAlocacoesAtivas() / stats.getTotalAlocacoes() * 100;
                sb.append("📊 Percentual Ativo: ").append(String.format("%.1f%%", percentualAtivas));
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            return "❌ ERRO ao obter estatísticas: " + e.getMessage();
        }
    }
}