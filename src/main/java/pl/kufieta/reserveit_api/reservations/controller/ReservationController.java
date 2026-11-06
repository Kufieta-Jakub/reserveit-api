package pl.kufieta.reserveit_api.reservations.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pl.kufieta.reserveit_api.reservations.dto.CreateReservationRequest;
import pl.kufieta.reserveit_api.reservations.dto.ReservationResponse;

import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;
import pl.kufieta.reserveit_api.reservations.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")

public class ReservationController {
    // Ciekawa zaleznosc final nie moze byc z Autowired, zaleta konstruktora ze nie da sie tego podmienic, ani zostawic jako null a tu da sie

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest createReservationRequest, Authentication authentication) {
        return reservationService.create(createReservationRequest,authentication.getName());
    }


}
