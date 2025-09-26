package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ProblemaValidator implements ConstraintValidator<ValidProblema, String> {
    
    private static final Pattern PALAVRAS_PROIBIDAS = Pattern.compile(
        "(?i).*(merda|porra|caralho|buceta|puta|fdp|desgraça|lixo|idiota|burro|estúpido).*"
    );
    
    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 500;
    
    private boolean required;
    
    @Override
    public void initialize(ValidProblema constraintAnnotation) {
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
            context.buildConstraintViolationWithTemplate("Descrição do problema é obrigatória")
                   .addConstraintViolation();
            return false;
        }
        
        // Se tem valor, valida o conteúdo
        if (value != null && !value.trim().isEmpty()) {
            String problema = value.trim();
            
            // Verifica o tamanho mínimo
            if (problema.length() < MIN_LENGTH) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Descrição do problema deve ter pelo menos " + MIN_LENGTH + " caracteres")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica o tamanho máximo
            if (problema.length() > MAX_LENGTH) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Descrição do problema deve ter no máximo " + MAX_LENGTH + " caracteres")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se contém palavras ofensivas
            if (PALAVRAS_PROIBIDAS.matcher(problema).matches()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Descrição do problema contém linguagem inadequada")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se não é só espaços ou caracteres especiais
            if (problema.replaceAll("[\\s\\p{Punct}]+", "").length() < 5) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Descrição do problema deve conter pelo menos 5 caracteres válidos")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
