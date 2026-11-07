package pl.kufieta.reserveit_api.reservations.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime time);

    @Query("""
            SELECT COUNT(r) FROM Reservation r
            WHERE r.facility.id = :facilityId
              AND r.status <> pl.kufieta.reserveit_api.reservations.entity.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    long countOverlapping(@Param("facilityId") Long facilityId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    @EntityGraph(attributePaths = "facility")
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.user.email = :email
              AND r.startTime >= :from
              AND r.startTime < :to
            """)
    Page<Reservation> findMine(@Param("email") String email,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               Pageable pageable);
}
