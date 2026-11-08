package pl.kufieta.reserveit_api.reservations.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pl.kufieta.reserveit_api.common.ConflictException;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.facilities.repository.FacilityRepository;
import pl.kufieta.reserveit_api.payments.repository.PaymentRepository;
import pl.kufieta.reserveit_api.reservations.dto.CreateReservationRequest;
import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;
import pl.kufieta.reserveit_api.users.entity.Role;
import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class ReservationConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private FacilityRepository facilityRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private Facility facility;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        reservationRepository.deleteAll();
        userRepository.deleteAll();
        facilityRepository.deleteAll();

        facility = facilityRepository.save(new Facility(
                "Kort testowy", new BigDecimal("50.00"),
                LocalTime.of(8, 0), LocalTime.of(22, 0)));
        userRepository.save(new User("test@test.pl", "hash", Role.USER));
    }

    @Test
    void onlyOneOfConcurrentReservationsForSameSlotSucceeds() throws Exception {
        int threads = 10;
        LocalDate day = LocalDate.now().plusDays(10);
        CreateReservationRequest request = new CreateReservationRequest(
                facility.getId(), day.atTime(10, 0), day.atTime(11, 0));

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            results.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    reservationService.create(request, "test@test.pl");
                    return true;
                } catch (ConflictException e) {
                    return false;
                }
            }));
        }

        ready.await();
        start.countDown();

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get()) {
                successes++;
            }
        }
        pool.shutdown();

        assertEquals(1, successes);
        assertEquals(1, reservationRepository.count());
    }
}
