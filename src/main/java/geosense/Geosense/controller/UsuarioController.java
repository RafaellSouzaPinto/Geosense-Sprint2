package geosense.Geosense.controller;

import geosense.Geosense.dto.UsuarioDTO;
import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String list(Model model) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/list";
    }

    @GetMapping("/novo")
    public String createForm(Model model) {
        model.addAttribute("usuario", new UsuarioDTO("", "", ""));
        model.addAttribute("tipos", TipoUsuario.values());
        return "usuarios/form";
    }

    @PostMapping
    public String create(@Valid UsuarioDTO dto,
                         BindingResult bindingResult,
                         @RequestParam(defaultValue = "MECANICO") TipoUsuario tipo,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", dto);
            model.addAttribute("tipos", TipoUsuario.values());
            return "usuarios/form";
        }
        Usuario u = new Usuario();
        u.setNome(dto.getNome());
        u.setEmail(dto.getEmail());
        u.setSenha(passwordEncoder.encode(dto.getSenha()));
        u.setTipo(tipo);
        usuarioRepository.save(u);
        redirectAttributes.addFlashAttribute("success", "Usuário criado");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        // Não preenche a senha para evitar exibir hash e forçar novo valor
        model.addAttribute("usuario", new UsuarioDTO(u.getNome(), u.getEmail(), ""));
        model.addAttribute("id", id);
        model.addAttribute("tipoAtual", u.getTipo());
        model.addAttribute("tipos", TipoUsuario.values());
        return "usuarios/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid UsuarioDTO dto,
                         BindingResult bindingResult,
                         @RequestParam(defaultValue = "MECANICO") TipoUsuario tipo,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", dto);
            model.addAttribute("id", id);
            model.addAttribute("tipoAtual", tipo);
            model.addAttribute("tipos", TipoUsuario.values());
            return "usuarios/form";
        }
        Usuario u = usuarioRepository.findById(id).orElseThrow();
        u.setNome(dto.getNome());
        u.setEmail(dto.getEmail());
        // Se alterou a senha, re-encode; para simplicidade sempre re-encode aqui
        u.setSenha(passwordEncoder.encode(dto.getSenha()));
        u.setTipo(tipo);
        usuarioRepository.save(u);
        redirectAttributes.addFlashAttribute("success", "Usuário atualizado");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/excluir")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Usuário removido");
        return "redirect:/usuarios";
    }
}


