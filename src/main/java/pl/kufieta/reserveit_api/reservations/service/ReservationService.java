package pl.kufieta.reserveit_api.reservations.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.common.ConflictException;
import pl.kufieta.reserveit_api.common.NotFoundException;
import pl.kufieta.reserveit_api.facilities.controller.repository.FacilityRepository;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.reservations.dto.CreateReservationRequest;
import pl.kufieta.reserveit_api.reservations.dto.ReservationResponse;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;
import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.repository.UserRepository;

@Service
public class ReservationService {
    // Testuje autowired aby nie pisac juz od razu wstrzyknąć do gotowe zależności

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReservationResponse create(CreateReservationRequest createReservationRequest, String userEmail) {
        if(!createReservationRequest.startTime().isBefore(createReservationRequest.endTime())){
            throw  new IllegalArgumentException("start time must be before end time");
        }
        if (!createReservationRequest.startTime().toLocalDate().equals(createReservationRequest.endTime().toLocalDate())) {
            throw new IllegalArgumentException("Reservation must start and end on the same day");
        }

        Facility facility = facilityRepository.findById(createReservationRequest.facilityId())
                .orElseThrow(() -> new IllegalArgumentException("facility id not found"));

        if(!facility.isActive())
        {
            throw  new IllegalArgumentException("facility is not active");
        }
        if (createReservationRequest.startTime().toLocalTime().isBefore(facility.getOpensAt())
                || createReservationRequest.endTime().toLocalTime().isAfter(facility.getClosesAt())) {
            throw new IllegalArgumentException("Reservation must fit within opening hours "
                    + facility.getOpensAt() + " - " + facility.getClosesAt());
        }
        if (reservationRepository.countOverlapping(
                facility.getId(), createReservationRequest.startTime(), createReservationRequest.endTime()) > 0) {
            throw new ConflictException("Facility is already booked in this time");
        }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Reservation reservation = new Reservation(
                facility, user, createReservationRequest.startTime(), createReservationRequest.endTime());
        try {
            return ReservationResponse.from(reservationRepository.save(reservation));
        }catch (DataIntegrityViolationException e){
            throw  new ConflictException("Facility is already booked in this time");
        }
    }


}
