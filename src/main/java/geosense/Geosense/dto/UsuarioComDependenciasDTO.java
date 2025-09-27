package geosense.Geosense.dto;

import geosense.Geosense.entity.Usuario;

public class UsuarioComDependenciasDTO {
    private Usuario usuario;
    private long alocacoesComoMecanico;
    private long alocacoesComoFinalizador;
    private boolean podeExcluir;

    public UsuarioComDependenciasDTO(Usuario usuario, long alocacoesComoMecanico, long alocacoesComoFinalizador) {
        this.usuario = usuario;
        this.alocacoesComoMecanico = alocacoesComoMecanico;
        this.alocacoesComoFinalizador = alocacoesComoFinalizador;
        this.podeExcluir = (alocacoesComoMecanico == 0 && alocacoesComoFinalizador == 0);
    }

    // Getters
    public Usuario getUsuario() {
        return usuario;
    }

    public long getAlocacoesComoMecanico() {
        return alocacoesComoMecanico;
    }

    public long getAlocacoesComoFinalizador() {
        return alocacoesComoFinalizador;
    }

    public boolean isPodeExcluir() {
        return podeExcluir;
    }

    // Delegar métodos do usuário para facilitar o acesso
    public Long getId() {
        return usuario.getId();
    }

    public String getNome() {
        return usuario.getNome();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public geosense.Geosense.entity.TipoUsuario getTipo() {
        return usuario.getTipo();
    }
}
