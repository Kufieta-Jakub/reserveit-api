package pl.kufieta.reserveit_api.facilities.dto;

import pl.kufieta.reserveit_api.facilities.entity.Facility;
import java.math.BigDecimal;
import java.time.LocalTime;

public record FacilityResponse(
        Long id, String name, BigDecimal pricePerHour,
        LocalTime opensAt, LocalTime closesAt, boolean active) {

    public static FacilityResponse from(Facility f) {
        return new FacilityResponse(f.getId(), f.getName(), f.getPricePerHour(),
                f.getOpensAt(), f.getClosesAt(), f.isActive());
    }
}