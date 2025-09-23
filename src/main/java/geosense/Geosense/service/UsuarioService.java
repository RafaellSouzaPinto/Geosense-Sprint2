package geosense.Geosense.service;

import geosense.Geosense.dto.CredentialsDTO;
import geosense.Geosense.dto.UsuarioDTO;
import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final String ADMIN_EMAIL = "mottu@gmail.com";
    private static final String ADMIN_PASSWORD = "Geosense@2025";

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public ResponseEntity<String> login(CredentialsDTO cred) {
        if (ADMIN_EMAIL.equals(cred.getEmail()) && ADMIN_PASSWORD.equals(cred.getSenha())) {
            Optional<Usuario> adminOpt = repo.findByTipo(TipoUsuario.ADMINISTRADOR);
            if (adminOpt.isPresent()) {
                return ResponseEntity.ok("Bem vindo ao modo administrador");
            } else {
                Usuario admin = new Usuario();
                admin.setNome("Administrador");
                admin.setEmail(ADMIN_EMAIL);
                admin.setSenha(ADMIN_PASSWORD);
                admin.setTipo(TipoUsuario.ADMINISTRADOR);
                repo.save(admin);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Administrador vinculado");
            }
        }

        Optional<Usuario> mecanicoOpt = repo
                .findByEmailAndSenhaAndTipo(cred.getEmail(), cred.getSenha(), TipoUsuario.MECANICO);

        return mecanicoOpt
                .map(u -> ResponseEntity.ok("Logado como mecânico: " + u.getNome()))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Credenciais inválidas"));
    }

    public Usuario register(UsuarioDTO dto) {
        if (ADMIN_EMAIL.equals(dto.getEmail()) && ADMIN_PASSWORD.equals(dto.getSenha())) {
            Optional<Usuario> adminExistente = repo.findByTipo(TipoUsuario.ADMINISTRADOR);
            return adminExistente.orElseThrow(() ->
                    new IllegalArgumentException("Administrador já existe. Use o login."));
        }

        Optional<Usuario> existente = repo
                .findByEmailOrNomeOrSenha(dto.getEmail(), dto.getNome(), dto.getSenha());

        if (existente.isPresent()) {
            throw new IllegalArgumentException("Usuário com e-mail, nome ou senha já existente.");
        }

        Usuario u = new Usuario();
        u.setNome(dto.getNome());
        u.setEmail(dto.getEmail());
        u.setSenha(dto.getSenha());
        u.setTipo(TipoUsuario.MECANICO);
        return repo.save(u);
    }

    public Optional<Usuario> findById(Long id) {
        return repo.findById(id);
    }

    public ResponseEntity<?> update(Long id, UsuarioDTO dto) {
        return repo.findById(id)
                .map(u -> {
                    u.setNome(dto.getNome());
                    u.setEmail(dto.getEmail());
                    u.setSenha(dto.getSenha());
                    repo.save(u);
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<Usuario> listAll() {
        return repo.findAll();
    }
}