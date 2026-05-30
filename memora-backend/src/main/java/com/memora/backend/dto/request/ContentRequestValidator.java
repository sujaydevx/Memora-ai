package com.memora.backend.dto.request;

import com.memora.backend.entity.ContentType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContentRequestValidator implements ConstraintValidator<ValidContentRequest, SaveContentRequest> {

    @Override
    public boolean isValid(SaveContentRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getType() == null) return true;

        context.disableDefaultConstraintViolation();

        if (request.getType() == ContentType.TEXT || request.getType() == ContentType.NOTE) {
            if (request.getRawContent() == null || request.getRawContent().isBlank()) {
                context.buildConstraintViolationWithTemplate(
                        "rawContent is required for TEXT and NOTE types"
                ).addConstraintViolation();
                return false;
            }
        }

        if (request.getType() == ContentType.IMAGE) {
            if (request.getMinioKey() == null || request.getMinioKey().isBlank()) {
                context.buildConstraintViolationWithTemplate(
                        "minioKey is required for IMAGE type"
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}