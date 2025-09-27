package geosense.Geosense.controller;

import geosense.Geosense.dto.MotoDTO;
import geosense.Geosense.dto.PatioDTO;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.Moto;
import geosense.Geosense.entity.StatusVaga;
import geosense.Geosense.entity.Vaga;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.VagaRepository;
import geosense.Geosense.service.PatioService;
import jakarta.transaction.Transactional;
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
    private final PatioService patioService;
    private final AlocacaoMotoRepository alocacaoRepository;

    public MotoController(MotoRepository motoRepository, VagaRepository vagaRepository, 
                         PatioService patioService, AlocacaoMotoRepository alocacaoRepository) {
        this.motoRepository = motoRepository;
        this.vagaRepository = vagaRepository;
        this.patioService = patioService;
        this.alocacaoRepository = alocacaoRepository;
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
            model.addAttribute("moto", dto);  // ✅ Adicionar o objeto moto para o Thymeleaf
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
    @Transactional
    public String update(@PathVariable Long id, @Valid MotoDTO dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        System.out.println("=== EDITANDO MOTO ID: " + id + " ===");
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
            model.addAttribute("moto", dto);  // ✅ Adicionar o objeto moto para o Thymeleaf
            model.addAttribute("id", id);
            model.addAttribute("patios", patioService.listarTodos());
            model.addAttribute("vagas", vagaRepository.findAll());
            
            // Recuperar pátio selecionado se houver vaga
            if (dto.getVagaId() != null) {
                Vaga vaga = vagaRepository.findById(dto.getVagaId()).orElse(null);
                if (vaga != null) {
                    model.addAttribute("patioSelecionado", vaga.getPatio().getId());
                }
            }
            return "motos/form";
        }
        
        try {
            Moto moto = motoRepository.findById(id).orElseThrow(() -> new RuntimeException("Moto não encontrada"));
            
            // Limpar vaga anterior se existir
            if (moto.getVaga() != null) {
                Vaga vagaAnterior = moto.getVaga();
                vagaAnterior.setMoto(null);
                vagaAnterior.setStatus(StatusVaga.DISPONIVEL);
                vagaRepository.save(vagaAnterior);
                moto.setVaga(null);
                System.out.println("Vaga " + vagaAnterior.getNumero() + " liberada");
            }
            
            // Atualizar dados da moto
            moto.setModelo(dto.getModelo());
            moto.setPlaca(dto.getPlaca() != null && !dto.getPlaca().isBlank() ? dto.getPlaca() : null);
            moto.setChassi(dto.getChassi() != null && !dto.getChassi().isBlank() ? dto.getChassi() : null);
            moto.setProblemaIdentificado(dto.getProblemaIdentificado() != null && !dto.getProblemaIdentificado().isBlank() ? dto.getProblemaIdentificado() : null);
            
            // Alocar nova vaga se fornecida
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
                
                // Alocar vaga
                moto.setVaga(v);
                v.setMoto(moto);
                v.setStatus(StatusVaga.OCUPADA);
                
                vagaRepository.save(v);
                System.out.println("✅ Vaga " + v.getNumero() + " ocupada pela moto " + moto.getModelo());
            }
            
            motoRepository.save(moto);
            System.out.println("✅ Moto atualizada com sucesso: " + moto.getModelo());
            redirectAttributes.addFlashAttribute("success", "Moto atualizada com sucesso");
        } catch (Exception e) {
            System.err.println("❌ Erro ao atualizar moto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar moto: " + e.getMessage());
        }
        return "redirect:/motos";
    }

    @PostMapping("/{id}/excluir")
    @Transactional
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Moto moto = motoRepository.findById(id).orElseThrow(() -> new RuntimeException("Moto não encontrada"));
            
            System.out.println("=== EXCLUINDO MOTO " + moto.getModelo() + " ===");
            
            // 1. Buscar TODAS as alocações da moto (ativas e finalizadas)
            List<AlocacaoMoto> todasAlocacoes = alocacaoRepository.findHistoricoByMoto(moto);
            System.out.println("Encontradas " + todasAlocacoes.size() + " alocações para a moto");
            
            // 2. Finalizar alocações ativas e liberar vagas
            List<AlocacaoMoto> alocacoesAtivas = todasAlocacoes.stream()
                    .filter(a -> a.getStatus() == AlocacaoMoto.StatusAlocacao.ATIVA)
                    .collect(java.util.stream.Collectors.toList());
            
            if (!alocacoesAtivas.isEmpty()) {
                System.out.println("Finalizando " + alocacoesAtivas.size() + " alocações ativas...");
                for (AlocacaoMoto alocacao : alocacoesAtivas) {
                    alocacao.finalizarAlocacao(AlocacaoMoto.StatusAlocacao.CANCELADA, 
                                             "Moto excluída do sistema", null);
                    alocacaoRepository.save(alocacao);
                    
                    // Liberar vaga
                    Vaga vaga = alocacao.getVaga();
                    vaga.setMoto(null);
                    vaga.setStatus(StatusVaga.DISPONIVEL);
                    vagaRepository.save(vaga);
                    System.out.println("Vaga " + vaga.getNumero() + " liberada");
                }
            }
            
            // 3. Excluir TODAS as alocações da moto (ativas e finalizadas)
            System.out.println("Excluindo " + todasAlocacoes.size() + " alocações da moto...");
            alocacaoRepository.deleteAll(todasAlocacoes);
            
            // 4. Excluir a moto
            motoRepository.delete(moto);
            System.out.println("✅ Moto excluída com sucesso: " + moto.getModelo());
            redirectAttributes.addFlashAttribute("success", "Moto excluída com sucesso. " + 
                                                (todasAlocacoes.size() > 0 ? todasAlocacoes.size() + " alocações foram removidas." : ""));
        } catch (Exception e) {
            System.err.println("❌ Erro ao excluir moto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir moto: " + e.getMessage());
        }
        return "redirect:/motos";
    }
}