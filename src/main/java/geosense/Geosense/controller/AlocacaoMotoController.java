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

/**
 * Controller SIMPLIFICADO para AlocaçãoMoto
 * Foco: UX simples e direta
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
     * LISTAR todas as alocações
     */
    @GetMapping
    public String listar(Model model) {
        List<AlocacaoMoto> alocacoes = alocacaoRepository.findAllWithDetails();
        System.out.println("=== LISTANDO ALOCAÇÕES ===");
        System.out.println("Total de alocações encontradas: " + alocacoes.size());
        alocacoes.forEach(a -> {
            System.out.println("- Alocação " + a.getId() + ": Moto " + a.getMoto().getModelo() + 
                             " na Vaga " + a.getVaga().getNumero() + " do Pátio " + a.getVaga().getPatio().getNomeUnidade());
        });
        
        model.addAttribute("alocacoes", alocacoes);
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
     * REMOVER alocação (desalocar moto)
     */
    @PostMapping("/{id}/remover")
    public String remover(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            alocacaoService.desalocar(id);
            redirectAttributes.addFlashAttribute("success", "Moto desalocada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao desalocar: " + e.getMessage());
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
     * ENDPOINT DE DEBUG: Desalocar todas as motos
     */
    @PostMapping("/debug/desalocar-todas")
    @ResponseBody
    public String desalocarTodasAsMotos() {
        try {
            List<AlocacaoMotoDTO> todasAlocacoes = alocacaoService.listarTodas();
            
            System.out.println("=== DESALOCANDO TODAS AS MOTOS ===");
            System.out.println("Total de alocações encontradas: " + todasAlocacoes.size());
            
            if (todasAlocacoes.isEmpty()) {
                return "❌ Nenhuma alocação encontrada para desalocar.";
            }
            
            for (AlocacaoMotoDTO alocacao : todasAlocacoes) {
                System.out.println("Desalocando: Moto " + alocacao.getMotoId() + " da Vaga " + alocacao.getVagaId());
                alocacaoService.desalocar(alocacao.getId());
            }
            
            System.out.println("✅ Todas as motos foram desalocadas!");
            return "✅ SUCESSO! " + todasAlocacoes.size() + " motos foram desalocadas. Agora você pode fazer novas alocações.";
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao desalocar motos: " + e.getMessage());
            e.printStackTrace();
            return "❌ ERRO: " + e.getMessage();
        }
    }
}