package geosense.Geosense.controller;

import geosense.Geosense.dto.MotoDTO;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.VagaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/motos")
public class MotoController {

    private final MotoRepository motoRepository;
    private final VagaRepository vagaRepository;

    public MotoController(MotoRepository motoRepository, VagaRepository vagaRepository) {
        this.motoRepository = motoRepository;
        this.vagaRepository = vagaRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Moto> motos = motoRepository.findAll();
        model.addAttribute("motos", motos);
        return "motos/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        model.addAttribute("moto", new MotoDTO(null, "", "", "", "", null));
        model.addAttribute("vagas", vagaRepository.findAll());
        model.addAttribute("id", null);
        return "motos/form";
    }

    @PostMapping
    public String create(@Valid MotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vagas", vagaRepository.findAll());
            return "motos/form";
        }
        Moto m = new Moto();
        m.setModelo(dto.getModelo());
        m.setPlaca(dto.getPlaca());
        m.setChassi(dto.getChassi());
        m.setProblemaIdentificado(dto.getProblemaIdentificado());
        if (dto.getVagaId() != null) {
            Vaga v = vagaRepository.findById(dto.getVagaId()).orElseThrow();
            m.setVaga(v);
            v.setMoto(m);
        }
        motoRepository.save(m);
        redirectAttributes.addFlashAttribute("success", "Moto criada");
        return "redirect:/motos";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        Moto m = motoRepository.findById(id).orElseThrow();
        model.addAttribute("moto", new MotoDTO(m.getId(), m.getModelo(), m.getPlaca(), m.getChassi(), m.getProblemaIdentificado(), m.getVaga() != null ? m.getVaga().getId() : null));
        model.addAttribute("id", id);
        model.addAttribute("vagas", vagaRepository.findAll());
        return "motos/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid MotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vagas", vagaRepository.findAll());
            return "motos/form";
        }
        Moto m = motoRepository.findById(id).orElseThrow();
        m.setModelo(dto.getModelo());
        m.setPlaca(dto.getPlaca());
        m.setChassi(dto.getChassi());
        m.setProblemaIdentificado(dto.getProblemaIdentificado());
        if (dto.getVagaId() != null) {
            Vaga v = vagaRepository.findById(dto.getVagaId()).orElseThrow();
            m.setVaga(v);
            v.setMoto(m);
        } else {
            if (m.getVaga() != null) {
                m.getVaga().setMoto(null);
            }
            m.setVaga(null);
        }
        motoRepository.save(m);
        redirectAttributes.addFlashAttribute("success", "Moto atualizada");
        return "redirect:/motos";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        motoRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Moto removida");
        return "redirect:/motos";
    }
}


