package geosense.Geosense.controller;

import geosense.Geosense.repository.UsuarioRepository;
import geosense.Geosense.repository.PatioRepository;
import geosense.Geosense.repository.MotoRepository;
import geosense.Geosense.repository.AlocacaoMotoRepository;
import geosense.Geosense.repository.VagaRepository;
import geosense.Geosense.entity.TipoUsuario;
import geosense.Geosense.entity.AlocacaoMoto;
import geosense.Geosense.entity.StatusVaga;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

/**
 * Controller para APIs do painel administrativo
 * Fornece dados reais do banco para o dashboard
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','MECANICO')")
public class AdminApiController {

    private final UsuarioRepository usuarioRepository;
    private final PatioRepository patioRepository;
    private final MotoRepository motoRepository;
    private final AlocacaoMotoRepository alocacaoRepository;
    private final VagaRepository vagaRepository;

    public AdminApiController(UsuarioRepository usuarioRepository,
                             PatioRepository patioRepository,
                             MotoRepository motoRepository,
                             AlocacaoMotoRepository alocacaoRepository,
                             VagaRepository vagaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.patioRepository = patioRepository;
        this.motoRepository = motoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.vagaRepository = vagaRepository;
    }

    /**
     * Dados gerais do dashboard
     */
    @GetMapping("/dashboard-data")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // Contar vagas livres e ocupadas usando o status correto das vagas (mesmo método do PatioService)
        long totalVagas = vagaRepository.count();
        
        long vagasOcupadas = patioRepository.findAll().stream()
            .flatMap(patio -> patio.getVagas().stream())
            .mapToLong(vaga -> vaga.getStatus() == StatusVaga.OCUPADA ? 1 : 0)
            .sum();
        
        long vagasLivres = patioRepository.findAll().stream()
            .flatMap(patio -> patio.getVagas().stream())
            .mapToLong(vaga -> vaga.getStatus() == StatusVaga.DISPONIVEL ? 1 : 0)
            .sum();

        // Contar motos disponíveis (sem alocação)
        long totalMotos = motoRepository.count();
        long motosAlocadas = alocacaoRepository.count();
        long motosDisponiveis = totalMotos - motosAlocadas;

        // Calcular taxa de ocupação
        double taxaOcupacao = totalVagas > 0 ? (double) vagasOcupadas / totalVagas * 100 : 0;

        data.put("vagasLivres", vagasLivres);
        data.put("vagasOcupadas", vagasOcupadas);
        data.put("motosDisponiveis", motosDisponiveis);
        data.put("taxaOcupacao", Math.round(taxaOcupacao));
        data.put("totalUsuarios", usuarioRepository.count());
        data.put("totalPatios", patioRepository.count());
        data.put("totalVagas", totalVagas);

        return ResponseEntity.ok(data);
    }

    /**
     * Dados dos pátios para gráficos
     */
    @GetMapping("/patios-data")
    public ResponseEntity<List<Map<String, Object>>> getPatiosData() {
        List<Map<String, Object>> patiosData = new ArrayList<>();

        patioRepository.findAll().forEach(patio -> {
            Map<String, Object> patioInfo = new HashMap<>();
            
            // Contar vagas do pátio
            long totalVagas = patio.getVagas().size();
            long vagasOcupadas = patio.getVagas().stream()
                .mapToLong(vaga -> vaga.getMoto() != null ? 1 : 0)
                .sum();
            long vagasLivres = totalVagas - vagasOcupadas;

            patioInfo.put("nome", patio.getNomeUnidade() != null ? patio.getNomeUnidade() : patio.getLocalizacao());
            patioInfo.put("totalVagas", totalVagas);
            patioInfo.put("vagasOcupadas", vagasOcupadas);
            patioInfo.put("vagasLivres", vagasLivres);
            patioInfo.put("endereco", patio.getEnderecoDetalhado() != null ? patio.getEnderecoDetalhado() : patio.getLocalizacao());

            patiosData.add(patioInfo);
        });

        return ResponseEntity.ok(patiosData);
    }

    /**
     * Notificações recentes do sistema - APENAS DADOS REAIS DO BANCO
     */
    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        List<Map<String, Object>> notifications = new ArrayList<>();

        // RETORNA SEMPRE LISTA VAZIA - SEM DADOS FICTÍCIOS
        // As notificações só aparecerão quando houver eventos reais registrados no banco
        // com campos de timestamp próprios

        return ResponseEntity.ok(notifications);
    }

    /**
     * Atividades recentes do sistema - APENAS DADOS REAIS COM TIMESTAMP
     */
    @GetMapping("/recent-activities")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();

        // APENAS alocações reais que existem no banco E que tenham data/hora válida
        List<AlocacaoMoto> alocacoes = alocacaoRepository.findAll();
        for (AlocacaoMoto alocacao : alocacoes) {
            // Só exibe se tiver dados completos E data/hora de alocação
            if (alocacao.getMoto() != null && 
                alocacao.getVaga() != null && 
                alocacao.getVaga().getPatio() != null &&
                alocacao.getDataHoraAlocacao() != null) {
                
                String motoInfo = alocacao.getMoto().getPlaca() != null ? 
                                 alocacao.getMoto().getPlaca() : 
                                 (alocacao.getMoto().getChassi() != null ? alocacao.getMoto().getChassi() : "Moto #" + alocacao.getMoto().getId());
                String patioInfo = alocacao.getVaga().getPatio().getNomeUnidade() != null ? 
                                  alocacao.getVaga().getPatio().getNomeUnidade() : 
                                  (alocacao.getVaga().getPatio().getLocalizacao() != null ? alocacao.getVaga().getPatio().getLocalizacao() : "Pátio #" + alocacao.getVaga().getPatio().getId());
                
                activities.add(createActivity(
                    "Alocação realizada",
                    "Moto " + motoInfo + " → " + patioInfo + " (Vaga " + alocacao.getVaga().getNumero() + ")",
                    alocacao.getDataHoraAlocacao(),
                    "map-marker-alt",
                    "info"
                ));
            }
        }

        // Se não há atividades reais com data válida, retornar lista vazia
        if (activities.isEmpty()) {
            return ResponseEntity.ok(activities);
        }

        // Ordenar por data (mais recentes primeiro) e limitar a 10
        activities.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("dateTime");
            LocalDateTime dateB = (LocalDateTime) b.get("dateTime");
            return dateB.compareTo(dateA);
        });

        return ResponseEntity.ok(activities.size() > 10 ? 
                                activities.subList(0, 10) : activities);
    }

    /**
     * Estatísticas detalhadas do sistema
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDetailedStats() {
        Map<String, Object> stats = new HashMap<>();

        // Estatísticas de usuários por tipo
        Map<String, Long> userStats = new HashMap<>();
        userStats.put("administradores", usuarioRepository.countByTipo(TipoUsuario.ADMINISTRADOR));
        userStats.put("mecanicos", usuarioRepository.countByTipo(TipoUsuario.MECANICO));

        // Estatísticas de motos
        Map<String, Object> motoStats = new HashMap<>();
        motoStats.put("total", motoRepository.count());
        motoStats.put("alocadas", alocacaoRepository.count());
        motoStats.put("disponiveis", motoRepository.count() - alocacaoRepository.count());

        // Estatísticas de pátios
        Map<String, Object> patioStats = new HashMap<>();
        patioStats.put("total", patioRepository.count());
        patioStats.put("totalVagas", vagaRepository.count());
        patioStats.put("vagasOcupadas", alocacaoRepository.count());

        stats.put("usuarios", userStats);
        stats.put("motos", motoStats);
        stats.put("patios", patioStats);

        return ResponseEntity.ok(stats);
    }

    /**
     * Últimas motos cadastradas no sistema
     */
    @GetMapping("/latest-motorcycles")
    public ResponseEntity<List<Map<String, Object>>> getLatestMotorcycles() {
        List<Map<String, Object>> motorcycles = new ArrayList<>();

        // Buscar todas as motos ordenadas por ID (proxy para data de cadastro)
        List<geosense.Geosense.entity.Moto> allMotos = motoRepository.findAll();
        
        // Reverter lista para mostrar as mais recentes primeiro
        java.util.Collections.reverse(allMotos);
        
        // Limitar a 4 motos mais recentes
        int limit = Math.min(4, allMotos.size());
        
        for (int i = 0; i < limit; i++) {
            geosense.Geosense.entity.Moto moto = allMotos.get(i);
            Map<String, Object> motoInfo = new HashMap<>();
            
            motoInfo.put("id", moto.getId());
            motoInfo.put("modelo", moto.getModelo() != null ? moto.getModelo() : "Modelo não informado");
            motoInfo.put("placa", moto.getPlaca() != null ? moto.getPlaca() : "Placa não informada");
            motoInfo.put("chassi", moto.getChassi() != null ? moto.getChassi() : "Chassi não informado");
            motoInfo.put("temVaga", moto.getVaga() != null);
            motoInfo.put("status", moto.getVaga() != null ? "Alocada" : "Disponível");
            
            if (moto.getVaga() != null) {
                motoInfo.put("vaga", moto.getVaga().getNumero());
                motoInfo.put("patio", moto.getVaga().getPatio().getNomeUnidade());
            }
            
            motorcycles.add(motoInfo);
        }

        return ResponseEntity.ok(motorcycles);
    }

    // Métodos auxiliares

    private Map<String, Object> createActivity(String action, String details, LocalDateTime time, String icon, String color) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("action", action);
        activity.put("details", details);
        activity.put("time", formatTimeAgo(time));
        activity.put("dateTime", time); // Para ordenação
        activity.put("icon", icon);
        activity.put("color", color);
        return activity;
    }

    private String formatTimeAgo(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(time, now).toMinutes();
        
        if (minutes < 1) return "agora";
        if (minutes < 60) return minutes + " minutos atrás";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + " horas atrás";
        
        long days = hours / 24;
        return days + " dias atrás";
    }
}
