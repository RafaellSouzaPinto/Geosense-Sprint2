package geosense.Geosense.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CredentialsDTO(
        @NotBlank(message = "Email e obrigatorio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Senha e obrigatoria")
        String senha
) {
    // Getters de compatibilidade
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
