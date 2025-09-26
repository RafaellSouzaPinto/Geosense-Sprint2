package geosense.Geosense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import geosense.Geosense.validation.ValidEmail;
import geosense.Geosense.validation.ValidSenha;

public record UsuarioDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter até 100 caracteres")
        String nome,

        @ValidEmail(required = true)
        String email,

        @ValidSenha(
            minLength = 8,
            maxLength = 128,
            requireUppercase = true,
            requireLowercase = true,
            requireDigit = true,
            requireSpecialChar = true,
            required = true
        )
        String senha
) {
    // Getters de compatibilidade com codigo legado
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
