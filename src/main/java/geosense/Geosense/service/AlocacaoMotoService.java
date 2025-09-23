package geosense.Geosense.service;

import geosense.Geosense.dto.AlocacaoMotoDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.Usuario;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlocacaoMotoService {

    @Autowired
    private AlocacaoMotoRepository alocacaoMotoRepository;

    @Autowired
    private MotoRepository motoRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AlocacaoMotoDTO alocar(AlocacaoMotoDTO dto) {
        Moto moto = motoRepository.findById(dto.getMotoId())
                .orElseThrow(() -> new RuntimeException("Moto não encontrada"));
        Vaga vaga = vagaRepository.findById(dto.getVagaId())
                .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));

        Usuario mecanico = null;
        if (dto.getMecanicoResponsavelId() != null) {
            mecanico = usuarioRepository.findById(dto.getMecanicoResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Mecânico não encontrado"));
        }

        AlocacaoMoto alocacao = new AlocacaoMoto();
        alocacao.setMoto(moto);
        alocacao.setVaga(vaga);
        alocacao.setMecanicoResponsavel(mecanico);
        alocacao.setDataHoraAlocacao(LocalDateTime.now());

        AlocacaoMoto salvo = alocacaoMotoRepository.save(alocacao);

        return new AlocacaoMotoDTO(
                salvo.getId(),
                salvo.getMoto().getId(),
                salvo.getVaga().getId(),
                salvo.getMecanicoResponsavel() != null ? salvo.getMecanicoResponsavel().getId() : null,
                salvo.getDataHoraAlocacao()
        );
    }

    public List<AlocacaoMotoDTO> listarTodos() {
        return alocacaoMotoRepository.findAll().stream()
                .map(a -> new AlocacaoMotoDTO(
                        a.getId(),
                        a.getMoto().getId(),
                        a.getVaga().getId(),
                        a.getMecanicoResponsavel() != null ? a.getMecanicoResponsavel().getId() : null,
                        a.getDataHoraAlocacao()))
                .collect(Collectors.toList());
    }

    public Optional<AlocacaoMotoDTO> buscarPorId(Long id) {
        return alocacaoMotoRepository.findById(id)
                .map(a -> new AlocacaoMotoDTO(
                        a.getId(),
                        a.getMoto().getId(),
                        a.getVaga().getId(),
                        a.getMecanicoResponsavel() != null ? a.getMecanicoResponsavel().getId() : null,
                        a.getDataHoraAlocacao()));
    }

    public void deletar(Long id) {
        alocacaoMotoRepository.deleteById(id);
    }
}
