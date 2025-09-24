package geosense.Geosense.controller;

import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.repository.PatioRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UsuarioRepository usuarioRepository;
    private final PatioRepository patioRepository;
    private final MotoRepository motoRepository;
    private final AlocacaoMotoRepository alocacaoMotoRepository;

    public HomeController(UsuarioRepository usuarioRepository,
                          PatioRepository patioRepository,
                          MotoRepository motoRepository,
                          AlocacaoMotoRepository alocacaoMotoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.patioRepository = patioRepository;
        this.motoRepository = motoRepository;
        this.alocacaoMotoRepository = alocacaoMotoRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("userCount", usuarioRepository.count());
        model.addAttribute("patioCount", patioRepository.count());
        model.addAttribute("motoCount", motoRepository.count());
        model.addAttribute("alocacaoCount", alocacaoMotoRepository.count());
        return "admin";
    }
}


