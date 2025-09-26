package geosense.Geosense.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CapacidadeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCapacidade {
    String message() default "Capacidade deve estar entre 1 e 10000";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min() default 1;
    int max() default 10000;
    boolean required() default true;
}
