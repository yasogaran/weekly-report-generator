package com.company.weeklyreports.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoursByTypeDTO {

    @NotBlank(message = "is required")
    private String taskType;

    @NotNull(message = "is required")
    private Double hours;
}
