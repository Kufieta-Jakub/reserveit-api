package pl.kufieta.reserveit_api.reservations.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReservationExpiryService expiryService;

    public ReservationExpiryScheduler(ReservationExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void run() {
        int expired = expiryService.expireOverdue();
        if (expired > 0) {
            log.info("Expired {} unpaid reservations", expired);
        }
    }
}