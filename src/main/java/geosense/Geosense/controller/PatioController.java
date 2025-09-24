package geosense.Geosense.controller;

import geosense.Geosense.entity.Patio;
import geosense.Geosense.repository.PatioRepository;
import jakarta.validation.Valid;
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

    public PatioController(PatioRepository patioRepository) {
        this.patioRepository = patioRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Patio> patios = patioRepository.findAll();
        model.addAttribute("patios", patios);
        return "patios/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        model.addAttribute("patio", new Patio());
        return "patios/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("patio") Patio patio,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patios/form";
        }
        patioRepository.save(patio);
        redirectAttributes.addFlashAttribute("success", "Pátio criado");
        return "redirect:/patios";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        Patio patio = patioRepository.findById(id).orElseThrow();
        model.addAttribute("patio", patio);
        model.addAttribute("id", id);
        return "patios/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("patio") Patio patio,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "patios/form";
        }
        Patio existing = patioRepository.findById(id).orElseThrow();
        existing.setLocalizacao(patio.getLocalizacao());
        patioRepository.save(existing);
        redirectAttributes.addFlashAttribute("success", "Pátio atualizado");
        return "redirect:/patios";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        patioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Pátio removido");
        return "redirect:/patios";
    }
}


