package geosense.Geosense.service;

import geosense.Geosense.dto.MotoDTO;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotoService {

    private final List<String> PROBLEMAS_VALIDOS = Arrays.asList(
            "reparos simples", "motor defeituoso", "danos estruturais"
    );

    @Autowired
    private MotoRepository motoRepository;

    @Autowired
    private VagaRepository vagaRepository;

    public MotoDTO registrar(MotoDTO dto) {
        validarRegrasNegocio(dto);

        Moto moto = new Moto();
        moto.setModelo(dto.getModelo());
        moto.setPlaca(dto.getPlaca() != null && !dto.getPlaca().isBlank() ? dto.getPlaca() : null);
        moto.setChassi(dto.getChassi() != null && !dto.getChassi().isBlank() ? dto.getChassi() : null);
        moto.setProblemaIdentificado(dto.getProblemaIdentificado());

        if (dto.getVagaId() != null) {
            Vaga vaga = vagaRepository.findById(dto.getVagaId())
                    .orElseThrow(() -> new RuntimeException("Vaga não encontrada"));
            moto.setVaga(vaga);
            vaga.setMoto(moto);
        }

        Moto salva = motoRepository.save(moto);
        return new MotoDTO(
                salva.getId(),
                salva.getModelo(),
                salva.getPlaca(),
                salva.getChassi(),
                salva.getProblemaIdentificado(),
                salva.getVaga() != null ? salva.getVaga().getId() : null
        );
    }

    private void validarRegrasNegocio(MotoDTO dto) {
        String problema = dto.getProblemaIdentificado();

        if (problema == null || problema.isBlank()) {
            throw new RuntimeException("O campo problemaIdentificado é obrigatório.");
        }

        if (!PROBLEMAS_VALIDOS.contains(problema.toLowerCase())) {
            throw new RuntimeException("Problema identificado inválido. Use: " + PROBLEMAS_VALIDOS);
        }

        boolean placaVazia = dto.getPlaca() == null || dto.getPlaca().isBlank();
        boolean chassiVazio = dto.getChassi() == null || dto.getChassi().isBlank();

        if (placaVazia && chassiVazio) {
            throw new RuntimeException("Informe a placa ou o chassi obrigatoriamente.");
        }
    }

    public List<MotoDTO> listar() {
        return motoRepository.findAll().stream()
                .map(m -> new MotoDTO(
                        m.getId(),
                        m.getModelo(),
                        m.getPlaca(),
                        m.getChassi(),
                        m.getProblemaIdentificado(),
                        m.getVaga() != null ? m.getVaga().getId() : null
                ))
                .collect(Collectors.toList());
    }

    public void remover(Long id) {
        motoRepository.deleteById(id);
    }
}
