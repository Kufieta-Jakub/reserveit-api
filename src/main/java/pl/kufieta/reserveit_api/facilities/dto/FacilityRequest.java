package pl.kufieta.reserveit_api.facilities.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record FacilityRequest (
    @NotBlank @Size(max = 120) String name,
    @NotNull @DecimalMin("0.0") BigDecimal pricePerHour,
    @NotNull LocalTime opensAt,
    @NotNull LocalTime closesAt){
}
