package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class UnidadeValidator implements ConstraintValidator<ValidUnidade, String> {
    
    private static final Pattern UNIDADE_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ0-9\\s\\-\\.]+$");
    private static final Pattern PALAVRAS_PROIBIDAS = Pattern.compile(
        "(?i).*(test|teste|admin|root|null|undefined|debug).*"
    );
    
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;
    
    private boolean required;
    
    @Override
    public void initialize(ValidUnidade constraintAnnotation) {
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
            context.buildConstraintViolationWithTemplate("Nome da unidade é obrigatório")
                   .addConstraintViolation();
            return false;
        }
        
        String unidade = value.trim();
        
        // Verifica o tamanho mínimo
        if (unidade.length() < MIN_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nome da unidade deve ter pelo menos " + MIN_LENGTH + " caracteres")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica o tamanho máximo
        if (unidade.length() > MAX_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nome da unidade deve ter no máximo " + MAX_LENGTH + " caracteres")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se contém apenas caracteres válidos
        if (!UNIDADE_PATTERN.matcher(unidade).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nome da unidade contém caracteres inválidos")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não contém palavras reservadas
        if (PALAVRAS_PROIBIDAS.matcher(unidade).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nome da unidade contém palavras não permitidas")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se não começa ou termina com espaço/hífen/ponto
        if (unidade.matches("^[\\s\\-\\.].*|.*[\\s\\-\\.]$")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Nome da unidade não pode começar ou terminar com espaço, hífen ou ponto")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
