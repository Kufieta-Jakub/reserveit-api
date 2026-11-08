CREATE TABLE reservations (
                              id BIGSERIAL PRIMARY KEY,
                              facility_id BIGINT NOT NULL REFERENCES facilities(id),
                              user_id BIGINT NOT NULL REFERENCES users(id),
                              start_time TIMESTAMP NOT NULL,
                              end_time TIMESTAMP NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              CONSTRAINT chk_reservation_time CHECK (end_time > start_time)
);

CREATE INDEX idx_reservations_facility_time
    ON reservations (facility_id, start_time, end_time);