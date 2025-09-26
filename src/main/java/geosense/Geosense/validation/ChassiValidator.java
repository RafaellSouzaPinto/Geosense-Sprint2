package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ChassiValidator implements ConstraintValidator<ValidChassi, String> {
    
    private static final Pattern CHASSI_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    // Chassi VIN padrão: 17 caracteres alfanuméricos, excluindo I, O, Q
    
    private boolean required;
    
    @Override
    public void initialize(ValidChassi constraintAnnotation) {
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
            context.buildConstraintViolationWithTemplate("Chassi é obrigatório")
                   .addConstraintViolation();
            return false;
        }
        
        // Se tem valor, valida o formato
        if (value != null && !value.trim().isEmpty()) {
            String chassi = value.trim().toUpperCase();
            
            // Verifica se tem exatamente 17 caracteres
            if (chassi.length() != 17) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Chassi deve ter exatamente 17 caracteres")
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se contém apenas caracteres válidos (sem I, O, Q)
            if (!CHASSI_PATTERN.matcher(chassi).matches()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Chassi contém caracteres inválidos (I, O, Q não são permitidos)")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
