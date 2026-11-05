package pl.kufieta.reserveit_api.facilities.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.facilities.dto.FacilityRequest;
import pl.kufieta.reserveit_api.facilities.dto.FacilityResponse;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.facilities.controller.repository.FacilityRepository;
import java.util.List;

@Service
public class FacilityService {
    private final FacilityRepository facilityRepository;
    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }
    @Transactional
    public FacilityResponse createFacility(FacilityRequest request) {
        if(!request.opensAt().isBefore(request.closesAt())) {
            throw new IllegalArgumentException("Opens at before closes at");
        }
        Facility saved = facilityRepository.save(new Facility(request.name(), request.pricePerHour(), request.opensAt(), request.closesAt()));
        return FacilityResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<FacilityResponse> findAll(){
        return facilityRepository.findAll().stream().map(FacilityResponse::from).toList();
    }

}
