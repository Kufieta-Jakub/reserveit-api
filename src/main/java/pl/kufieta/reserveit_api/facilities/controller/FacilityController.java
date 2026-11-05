package pl.kufieta.reserveit_api.facilities.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.kufieta.reserveit_api.facilities.dto.FacilityRequest;
import pl.kufieta.reserveit_api.facilities.dto.FacilityResponse;

import pl.kufieta.reserveit_api.facilities.service.FacilityService;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {
    private final FacilityService service;

    public FacilityController(FacilityService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityResponse create(@Valid @RequestBody FacilityRequest request) {
        return service.createFacility(request);
    }
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<FacilityResponse> findAll() {
        return service.findAll();
    }

}
