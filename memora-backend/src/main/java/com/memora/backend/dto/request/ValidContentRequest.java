package com.memora.backend.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContentRequestValidator.class)
public @interface ValidContentRequest {
    String message() default "Invalid content request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}