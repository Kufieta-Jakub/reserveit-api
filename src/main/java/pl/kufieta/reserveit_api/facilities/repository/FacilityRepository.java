package pl.kufieta.reserveit_api.facilities.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.kufieta.reserveit_api.facilities.entity.Facility;

import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Facility f WHERE f.id = :id")
    Optional<Facility> findByIdForUpdate(@Param("id") Long id);
}
