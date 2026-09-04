package com.company.weeklyreports.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@code @JsonProperty("isKeyIssue")} is required, not decorative — a bare Lombok
 * getter/setter pair for this field name serializes as "keyIssue" and, worse, throws
 * UnrecognizedPropertyException deserializing the frontend's actual "isKeyIssue" payload
 * (verified directly against Jackson; see UserDTO's javadoc for the full explanation).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockerDTO {

    @NotBlank(message = "is required")
    private String description;

    @JsonProperty("isKeyIssue")
    private boolean isKeyIssue;
}
