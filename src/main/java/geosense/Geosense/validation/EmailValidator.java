package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern DOMINIOS_TEMPORARIOS = Pattern.compile(
        "(?i).*(10minutemail|guerrillamail|mailinator|tempmail|yopmail|throwaway|trash|fake).*"
    );
    
    private boolean required;
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Se não é obrigatório e está vazio, é válido
        if (!required && (value == null || value.trim().isEmpty())) {
            return true;
        }
        
        // Se é obrigatório e está vazio, é inválido
        if (required && (value == null || value.trim().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email é obrigatório")
                   .addConstraintViolation();
            return false;
        }
        
        String email = value.trim().toLowerCase();
        
        // Verifica o tamanho
        if (email.length() > 254) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email muito longo (máximo 254 caracteres)")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica formato básico
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Formato de email inválido")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não é domínio temporário
        if (DOMINIOS_TEMPORARIOS.matcher(email).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Emails temporários não são permitidos")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se a parte local não é muito longa
        String[] parts = email.split("@");
        if (parts.length == 2) {
            if (parts[0].length() > 64) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Parte local do email muito longa (máximo 64 caracteres)")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se não começa ou termina com ponto
            if (parts[0].startsWith(".") || parts[0].endsWith(".")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Email não pode começar ou terminar com ponto")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se não tem pontos consecutivos
            if (parts[0].contains("..")) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Email não pode ter pontos consecutivos")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
