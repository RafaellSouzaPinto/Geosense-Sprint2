package geosense.Geosense.controller;

import geosense.Geosense.dto.PatioDTO;
import geosense.Geosense.entity.Patio;
import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.PatioRepository;
import geosense.Geosense.repository.VagaRepository;
import geosense.Geosense.service.PatioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patios")
public class PatioController {

    private final PatioRepository patioRepository;
    private final VagaRepository vagaRepository;
    private final PatioService patioService;

    public PatioController(PatioRepository patioRepository, VagaRepository vagaRepository, PatioService patioService) {
        this.patioRepository = patioRepository;
        this.vagaRepository = vagaRepository;
        this.patioService = patioService;
    }

    @GetMapping
    public String list(Model model) {
        List<PatioDTO> patios = patioService.listarTodos();
        model.addAttribute("patios", patios);
        return "patios/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        PatioDTO patio = new PatioDTO(null, "", "", "", null, null, null, null);
        model.addAttribute("patio", patio);
        return "patios/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("patio") PatioDTO patioDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patios/form";
        }
        try {
            patioService.criarPatio(patioDTO);
            redirectAttributes.addFlashAttribute("success", "Pátio criado com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patios";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        PatioDTO patio = patioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pátio não encontrado"));
        model.addAttribute("patio", patio);
        model.addAttribute("id", id);
        return "patios/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("patio") PatioDTO patioDTO,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patios/form";
        }
        try {
            patioService.atualizar(id, patioDTO);
            redirectAttributes.addFlashAttribute("success", "Pátio atualizado com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patios";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            patioService.deletar(id);
            redirectAttributes.addFlashAttribute("success", "Pátio removido com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patios";
    }

    // API endpoint para buscar vagas disponíveis por pátio
    @GetMapping("/{patioId}/vagas-disponiveis")
    @ResponseBody
    public ResponseEntity<List<Vaga>> getVagasDisponiveis(@PathVariable Long patioId) {
        try {
            List<Vaga> vagasDisponiveis = vagaRepository.findVagasDisponiveisByPatioId(patioId);
            System.out.println("=== VAGAS DISPONÍVEIS ===");
            System.out.println("Pátio ID: " + patioId);
            System.out.println("Total de vagas disponíveis: " + vagasDisponiveis.size());
            
            vagasDisponiveis.forEach(vaga -> {
                System.out.println("Vaga " + vaga.getNumero() + " - Status: " + vaga.getStatus() + " - Moto: " + (vaga.getMoto() != null ? vaga.getMoto().getId() : "null"));
            });
            
            return ResponseEntity.ok(vagasDisponiveis);
        } catch (Exception e) {
            System.err.println("Erro ao buscar vagas disponíveis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    // API endpoint para listar todos os pátios (para AJAX)
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<List<PatioDTO>> getAllPatios() {
        List<PatioDTO> patios = patioService.listarTodos();
        return ResponseEntity.ok(patios);
    }

    // Endpoint de debug para verificar estado das vagas
    @GetMapping("/{patioId}/debug")
    @ResponseBody
    public ResponseEntity<String> debugPatioVagas(@PathVariable Long patioId) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== DEBUG PÁTIO ").append(patioId).append(" ===\n");
        
        List<Vaga> todasVagas = vagaRepository.findByPatioIdOrderByNumeroAsc(patioId);
        debug.append("Total de vagas: ").append(todasVagas.size()).append("\n");
        
        for (Vaga vaga : todasVagas) {
            debug.append("Vaga ").append(vaga.getNumero())
                  .append(" - Status: ").append(vaga.getStatus())
                  .append(" - Moto: ").append(vaga.getMoto() != null ? vaga.getMoto().getId() + " (" + vaga.getMoto().getModelo() + ")" : "null")
                  .append("\n");
        }
        
        long ocupadas = vagaRepository.countByPatioIdAndStatus(patioId, StatusVaga.OCUPADA);
        long disponiveis = vagaRepository.countByPatioIdAndStatus(patioId, StatusVaga.DISPONIVEL);
        
        debug.append("\nContadores:\n");
        debug.append("Ocupadas: ").append(ocupadas).append("\n");
        debug.append("Disponíveis: ").append(disponiveis).append("\n");
        
        return ResponseEntity.ok(debug.toString());
    }
}