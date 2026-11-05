package pl.kufieta.reserveit_api.facilities.controller.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kufieta.reserveit_api.facilities.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility,Long> {
}
