package pl.kufieta.reserveit_api.reservations.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.users.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Table(name = "reservations")
@Entity
public class Reservation {
    public static final long PAYMENT_TIMEOUT_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Reservation(Facility facility, User user, LocalDateTime startTime, LocalDateTime endTime) {
        this.facility = facility;
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusMinutes(PAYMENT_TIMEOUT_MINUTES);
    }
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
