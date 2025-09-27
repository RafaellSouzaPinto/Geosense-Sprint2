package geosense.Geosense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import geosense.Geosense.validation.ValidEmail;
import geosense.Geosense.validation.ValidSenha;

public record UsuarioEditDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter até 100 caracteres")
        String nome,

        @ValidEmail(required = true)
        String email,

        @ValidSenha(
            minLength = 6,
            maxLength = 128,
            requireUppercase = false,
            requireLowercase = false,
            requireDigit = false,
            requireSpecialChar = false,
            required = false
        )
        String senha
) {
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
}
