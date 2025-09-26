package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SenhaValidator implements ConstraintValidator<ValidSenha, String> {
    
    private static final Pattern SENHAS_COMUNS = Pattern.compile(
        "(?i).*(123456|password|senha|admin|qwerty|letmein|welcome|monkey|dragon|master|abc123|123123|password123|senha123|12345678|1234567890|00000000|11111111).*"
    );
    
    private static final Pattern SEQUENCIAS = Pattern.compile(
        ".*(012|123|234|345|456|567|678|789|890|987|876|765|654|543|432|321|210|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz|zyx|yxw|xwv|wvu|vut|uts|tsr|srq|rqp|qpo|pon|onm|nml|mlk|lkj|kji|jih|ihg|hgf|gfe|fed|edc|dcb|cba).*"
    );
    
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;
    private boolean required;
    
    @Override
    public void initialize(ValidSenha constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Se não é obrigatório e está vazio, é válido
        if (!required && (value == null || value.isEmpty())) {
            return true;
        }
        
        // Se é obrigatório e está vazio, é inválido
        if (required && (value == null || value.isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha é obrigatória")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica o tamanho mínimo
        if (value.length() < minLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve ter pelo menos " + minLength + " caracteres")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica o tamanho máximo
        if (value.length() > maxLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve ter no máximo " + maxLength + " caracteres")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não é uma senha comum
        if (SENHAS_COMUNS.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha muito comum, escolha uma senha mais segura")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não contém sequências
        if (SEQUENCIAS.matcher(value.toLowerCase()).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha não pode conter sequências de caracteres")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se tem letra maiúscula
        if (requireUppercase && !value.matches(".*[A-Z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve conter pelo menos uma letra maiúscula")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se tem letra minúscula
        if (requireLowercase && !value.matches(".*[a-z].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve conter pelo menos uma letra minúscula")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se tem dígito
        if (requireDigit && !value.matches(".*[0-9].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve conter pelo menos um número")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se tem caractere especial
        if (requireSpecialChar && !value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{}|;':\"\\\\,.<>/?)")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não tem mais de 3 caracteres iguais consecutivos
        if (value.matches(".*(.)\\1{3,}.*")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Senha não pode ter mais de 3 caracteres iguais consecutivos")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
