ALTER TABLE reservations ADD COLUMN expires_at TIMESTAMP;

CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          reservation_id BIGINT NOT NULL REFERENCES reservations(id),
                          amount NUMERIC(10,2) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          external_id VARCHAR(100) NOT NULL UNIQUE,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_reservation ON payments (reservation_id);