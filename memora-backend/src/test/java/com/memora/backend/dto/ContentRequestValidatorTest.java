package com.memora.backend.dto;

import com.memora.backend.dto.request.SaveContentRequest;
import com.memora.backend.entity.ContentType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ContentRequestValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void textTypeWithBlankRawContent_failsValidation() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("");

        var violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        System.out.println("Violations: " + violations);
    }

    @Test
    void textTypeWithValidRawContent_passesValidation() {
        SaveContentRequest request = new SaveContentRequest();
        request.setType(ContentType.TEXT);
        request.setRawContent("Valid content here");

        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}