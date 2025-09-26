package geosense.Geosense.controller;

import geosense.Geosense.dto.MotoDTO;
import geosense.Geosense.dto.PatioDTO;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.MotoRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Controller
@RequestMapping("/motos")
public class MotoController {

    private final MotoRepository motoRepository;
    private final VagaRepository vagaRepository;
    private final PatioRepository patioRepository;
    private final PatioService patioService;

    public MotoController(MotoRepository motoRepository, VagaRepository vagaRepository, 
                         PatioRepository patioRepository, PatioService patioService) {
        this.motoRepository = motoRepository;
        this.vagaRepository = vagaRepository;
        this.patioRepository = patioRepository;
        this.patioService = patioService;
    }

    @GetMapping
    public String list(Model model) {
        List<Moto> motos = motoRepository.findAll();
        model.addAttribute("motos", motos);
        return "motos/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        List<PatioDTO> patios = patioService.listarTodos();
        List<Vaga> vagas = vagaRepository.findAll();
        
        System.out.println("=== FORMULÁRIO NOVA MOTO ===");
        System.out.println("Pátios disponíveis: " + patios.size());
        patios.forEach(p -> System.out.println("- Pátio " + p.getId() + ": " + p.getNomeUnidade() + " (" + p.getVagasDisponiveis() + " vagas disponíveis)"));
        System.out.println("Total de vagas: " + vagas.size());
        
        model.addAttribute("moto", new MotoDTO(null, "", "", "", "", null));
        model.addAttribute("patios", patios);
        model.addAttribute("vagas", vagas);
        model.addAttribute("id", null);
        return "motos/form";
    }

    @PostMapping
    @Transactional
    public String create(@Valid MotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        System.out.println("=== CRIANDO MOTO ===");
        System.out.println("Modelo: " + dto.getModelo());
        System.out.println("Placa: " + dto.getPlaca());
        System.out.println("Chassi: " + dto.getChassi());
        System.out.println("Problema: " + dto.getProblemaIdentificado());
        System.out.println("VagaId: " + dto.getVagaId());
        
        // Validação customizada: placa OU chassi deve ser informado
        boolean placaVazia = dto.getPlaca() == null || dto.getPlaca().trim().isEmpty();
        boolean chassiVazio = dto.getChassi() == null || dto.getChassi().trim().isEmpty();
        
        if (placaVazia && chassiVazio) {
            bindingResult.rejectValue("placa", "required", "Informe a placa ou o chassi obrigatoriamente");
        }
        
        if (bindingResult.hasErrors()) {
            System.out.println("Erros de validação:");
            bindingResult.getAllErrors().forEach(error -> System.out.println("- " + error.getDefaultMessage()));
            model.addAttribute("patios", patioService.listarTodos());
            model.addAttribute("vagas", vagaRepository.findAll());
            return "motos/form";
        }
        
        try {
            Moto m = new Moto();
            m.setModelo(dto.getModelo());
            m.setPlaca(dto.getPlaca() != null && !dto.getPlaca().isBlank() ? dto.getPlaca() : null);
            m.setChassi(dto.getChassi() != null && !dto.getChassi().isBlank() ? dto.getChassi() : null);
            m.setProblemaIdentificado(dto.getProblemaIdentificado() != null && !dto.getProblemaIdentificado().isBlank() ? dto.getProblemaIdentificado() : null);
            
            if (dto.getVagaId() != null) {
                System.out.println("Tentando alocar vaga ID: " + dto.getVagaId());
                Vaga v = vagaRepository.findById(dto.getVagaId()).orElseThrow(() -> 
                    new RuntimeException("Vaga não encontrada"));
                
                System.out.println("Vaga encontrada: " + v.getNumero() + " - Status: " + v.getStatus() + " - Moto atual: " + (v.getMoto() != null ? v.getMoto().getId() : "null"));
                
                // Verificar se a vaga está disponível E não tem moto
                if (v.getStatus() != StatusVaga.DISPONIVEL) {
                    throw new RuntimeException("Vaga " + v.getNumero() + " não está com status DISPONÍVEL (Status atual: " + v.getStatus() + ")");
                }
                
                if (v.getMoto() != null) {
                    throw new RuntimeException("Vaga " + v.getNumero() + " já tem uma moto alocada (Moto ID: " + v.getMoto().getId() + ")");
                }
                
                // Verificar se já existe outra moto nesta vaga (double check)
                if (motoRepository.existsByVagaId(dto.getVagaId())) {
                    throw new RuntimeException("Já existe uma moto registrada nesta vaga no sistema");
                }
                
                // Salvar moto primeiro
                Moto motoSalva = motoRepository.save(m);
                System.out.println("Moto salva com ID: " + motoSalva.getId());
                
                // Depois alocar vaga
                motoSalva.setVaga(v);
                v.setMoto(motoSalva);
                v.setStatus(StatusVaga.OCUPADA);
                
                // Salvar vaga e moto com relacionamento
                vagaRepository.save(v);
                motoRepository.save(motoSalva);
                System.out.println("✅ Vaga " + v.getNumero() + " ocupada pela moto " + motoSalva.getModelo());
            } else {
                System.out.println("Moto criada sem vaga");
                motoRepository.save(m);
            }
            System.out.println("✅ Moto salva com sucesso: " + m.getModelo());
            redirectAttributes.addFlashAttribute("success", "Moto criada com sucesso");
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar moto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erro ao criar moto: " + e.getMessage());
        }
        return "redirect:/motos";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        Moto m = motoRepository.findById(id).orElseThrow();
        model.addAttribute("moto", new MotoDTO(m.getId(), m.getModelo(), m.getPlaca(), m.getChassi(), m.getProblemaIdentificado(), m.getVaga() != null ? m.getVaga().getId() : null));
        model.addAttribute("id", id);
        model.addAttribute("patios", patioService.listarTodos());
        model.addAttribute("vagas", vagaRepository.findAll());
        model.addAttribute("patioSelecionado", m.getVaga() != null ? m.getVaga().getPatio().getId() : null);
        return "motos/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid MotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("patios", patioService.listarTodos());
            model.addAttribute("vagas", vagaRepository.findAll());
            return "motos/form";
        }
        
        try {
            Moto m = motoRepository.findById(id).orElseThrow();
            Vaga vagaAnterior = m.getVaga(); // Guardar vaga anterior
            
            m.setModelo(dto.getModelo());
            m.setPlaca(dto.getPlaca() != null && !dto.getPlaca().isBlank() ? dto.getPlaca() : null);
            m.setChassi(dto.getChassi() != null && !dto.getChassi().isBlank() ? dto.getChassi() : null);
            m.setProblemaIdentificado(dto.getProblemaIdentificado() != null && !dto.getProblemaIdentificado().isBlank() ? dto.getProblemaIdentificado() : null);
            
            // Liberar vaga anterior se houver
            if (vagaAnterior != null) {
                vagaAnterior.setMoto(null);
                vagaAnterior.setStatus(StatusVaga.DISPONIVEL);
                vagaRepository.save(vagaAnterior);
                System.out.println("Vaga " + vagaAnterior.getNumero() + " liberada");
            }
            
            if (dto.getVagaId() != null) {
                Vaga v = vagaRepository.findById(dto.getVagaId()).orElseThrow();
                
                // Verificar se a nova vaga está disponível
                if (v.getStatus() != StatusVaga.DISPONIVEL || v.getMoto() != null) {
                    throw new RuntimeException("Vaga " + v.getNumero() + " já está ocupada");
                }
                
                m.setVaga(v);
                v.setMoto(m);
                v.setStatus(StatusVaga.OCUPADA);
                vagaRepository.save(v);
                System.out.println("Vaga " + v.getNumero() + " ocupada pela moto " + m.getModelo());
            } else {
                m.setVaga(null);
            }
            motoRepository.save(m);
            redirectAttributes.addFlashAttribute("success", "Moto atualizada com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/motos";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Moto moto = motoRepository.findById(id).orElseThrow();
            
            // Liberar vaga se a moto estiver alocada
            if (moto.getVaga() != null) {
                Vaga vaga = moto.getVaga();
                vaga.setMoto(null);
                vaga.setStatus(StatusVaga.DISPONIVEL);
                vagaRepository.save(vaga);
                System.out.println("Vaga " + vaga.getNumero() + " liberada ao excluir moto " + moto.getModelo());
            }
            
            motoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Moto removida com sucesso");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/motos";
    }

    // Endpoint de debug para ver o estado das motos
    @GetMapping("/debug")
    @ResponseBody
    public ResponseEntity<String> debugMotos() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== DEBUG MOTOS ===\n");
        
        List<Moto> todasMotos = motoRepository.findAll();
        debug.append("Total de motos: ").append(todasMotos.size()).append("\n\n");
        
        for (Moto moto : todasMotos) {
            debug.append("Moto ID: ").append(moto.getId())
                  .append(" - Modelo: ").append(moto.getModelo())
                  .append(" - Placa: ").append(moto.getPlaca() != null ? moto.getPlaca() : "null")
                  .append(" - Chassi: ").append(moto.getChassi() != null ? moto.getChassi() : "null")
                  .append(" - Problema: ").append(moto.getProblemaIdentificado() != null ? moto.getProblemaIdentificado() : "null");
            
            if (moto.getVaga() != null) {
                debug.append(" - Vaga: ").append(moto.getVaga().getNumero())
                      .append(" (Pátio: ").append(moto.getVaga().getPatio().getId()).append(")");
            } else {
                debug.append(" - Vaga: null");
            }
            debug.append("\n");
        }
        
        return ResponseEntity.ok(debug.toString());
    }
}