package pl.kufieta.reserveit_api.reservations.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import pl.kufieta.reserveit_api.reservations.dto.CreateReservationRequest;
import pl.kufieta.reserveit_api.reservations.dto.ReservationCreatedResponse;
import pl.kufieta.reserveit_api.reservations.dto.ReservationResponse;

import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;
import pl.kufieta.reserveit_api.reservations.service.ReservationService;

import java.time.LocalDate;

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
    public ReservationCreatedResponse create(@Valid @RequestBody CreateReservationRequest createReservationRequest, Authentication authentication) {
        return reservationService.create(createReservationRequest,authentication.getName());
    }


    @GetMapping("/my")
    public PagedModel<ReservationResponse> myReservations(
            Authentication authentication,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable) {
        return new PagedModel<>(
                reservationService.findMine(authentication.getName(), from, to, pageable));
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id, Authentication authentication) {
        return reservationService.cancel(id, authentication.getName());
    }



}
