package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CapacidadeValidator implements ConstraintValidator<ValidCapacidade, Integer> {
    
    private int min;
    private int max;
    private boolean required;
    
    @Override
    public void initialize(ValidCapacidade constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        // Se não é obrigatório e está null, é válido
        if (!required && value == null) {
            return true;
        }
        
        // Se é obrigatório e está null, é inválido
        if (required && value == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Capacidade é obrigatória")
                   .addConstraintViolation();
            return false;
        }
        
        // Se tem valor, valida os limites
        if (value != null) {
            // Verifica se é menor que o mínimo
            if (value < min) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Capacidade deve ser no mínimo " + min)
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se é maior que o máximo
            if (value > max) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Capacidade deve ser no máximo " + max)
                       .addConstraintViolation();
                return false;
            }
            
            // Verifica se é um número válido (não negativo)
            if (value <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Capacidade deve ser um número positivo")
                       .addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}
