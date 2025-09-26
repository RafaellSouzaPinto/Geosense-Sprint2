package geosense.Geosense.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PlacaValidator implements ConstraintValidator<ValidPlaca, String> {
    
    private static final Pattern PLACA_ANTIGA = Pattern.compile("^[A-Z]{3}-?[0-9]{4}$");
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    
    private boolean required;
    
    @Override
    public void initialize(ValidPlaca constraintAnnotation) {
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
            context.buildConstraintViolationWithTemplate("Placa é obrigatória")
                   .addConstraintViolation();
            return false;
        }
        
        String placa = value.trim().toUpperCase().replace("-", "");
        
        // Verifica se tem o tamanho correto
        if (placa.length() != 7) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Placa deve ter 7 caracteres (formato ABC-1234 ou ABC1D23)")
                   .addConstraintViolation();
            return false;
        }
        
        // Verifica se segue o padrão da placa antiga ou Mercosul
        boolean isPlacaAntiga = PLACA_ANTIGA.matcher(value.toUpperCase()).matches();
        boolean isPlacaMercosul = PLACA_MERCOSUL.matcher(placa).matches();
        
        if (!isPlacaAntiga && !isPlacaMercosul) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Placa deve seguir o formato brasileiro (ABC-1234 ou ABC1D23)")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
