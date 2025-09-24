package geosense.Geosense.controller;

import geosense.Geosense.dto.AlocacaoMotoDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.repository.VagaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/alocacoes")
public class AlocacaoMotoController {

    private final AlocacaoMotoRepository alocacaoRepo;
    private final MotoRepository motoRepo;
    private final VagaRepository vagaRepo;
    private final UsuarioRepository usuarioRepo;

    public AlocacaoMotoController(AlocacaoMotoRepository alocacaoRepo,
                                  MotoRepository motoRepo,
                                  VagaRepository vagaRepo,
                                  UsuarioRepository usuarioRepo) {
        this.alocacaoRepo = alocacaoRepo;
        this.motoRepo = motoRepo;
        this.vagaRepo = vagaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping
    public String list(Model model) {
        List<AlocacaoMoto> alocacoes = alocacaoRepo.findAll();
        model.addAttribute("alocacoes", alocacoes);
        return "alocacoes/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        model.addAttribute("alocacao", new AlocacaoMotoDTO(null, null, null, null, null));
        model.addAttribute("motos", motoRepo.findAll());
        model.addAttribute("vagas", vagaRepo.findAll());
        model.addAttribute("mecanicos", usuarioRepo.findAll());
        return "alocacoes/form";
    }

    @PostMapping
    public String create(@Valid AlocacaoMotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("motos", motoRepo.findAll());
            model.addAttribute("vagas", vagaRepo.findAll());
            model.addAttribute("mecanicos", usuarioRepo.findAll());
            return "alocacoes/form";
        }
        AlocacaoMoto a = new AlocacaoMoto();
        Moto m = motoRepo.findById(dto.getMotoId()).orElseThrow();
        Vaga v = vagaRepo.findById(dto.getVagaId()).orElseThrow();
        Usuario u = dto.getMecanicoResponsavelId() != null ? usuarioRepo.findById(dto.getMecanicoResponsavelId()).orElse(null) : null;
        a.setMoto(m);
        a.setVaga(v);
        a.setMecanicoResponsavel(u);
        a.setDataHoraAlocacao(LocalDateTime.now());
        alocacaoRepo.save(a);
        redirectAttributes.addFlashAttribute("success", "Alocação criada");
        return "redirect:/alocacoes";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        AlocacaoMoto a = alocacaoRepo.findById(id).orElseThrow();
        model.addAttribute("alocacao", new AlocacaoMotoDTO(a.getId(), a.getMoto().getId(), a.getVaga().getId(), a.getMecanicoResponsavel() != null ? a.getMecanicoResponsavel().getId() : null, a.getDataHoraAlocacao()));
        model.addAttribute("id", id);
        model.addAttribute("motos", motoRepo.findAll());
        model.addAttribute("vagas", vagaRepo.findAll());
        model.addAttribute("mecanicos", usuarioRepo.findAll());
        return "alocacoes/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid AlocacaoMotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("motos", motoRepo.findAll());
            model.addAttribute("vagas", vagaRepo.findAll());
            model.addAttribute("mecanicos", usuarioRepo.findAll());
            return "alocacoes/form";
        }
        AlocacaoMoto a = alocacaoRepo.findById(id).orElseThrow();
        Moto m = motoRepo.findById(dto.getMotoId()).orElseThrow();
        Vaga v = vagaRepo.findById(dto.getVagaId()).orElseThrow();
        Usuario u = dto.getMecanicoResponsavelId() != null ? usuarioRepo.findById(dto.getMecanicoResponsavelId()).orElse(null) : null;
        a.setMoto(m);
        a.setVaga(v);
        a.setMecanicoResponsavel(u);
        alocacaoRepo.save(a);
        redirectAttributes.addFlashAttribute("success", "Alocação atualizada");
        return "redirect:/alocacoes";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        alocacaoRepo.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Alocação removida");
        return "redirect:/alocacoes";
    }
}


