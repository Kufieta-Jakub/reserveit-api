CREATE TABLE facilities (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(120) NOT NULL,
                            price_per_hour NUMERIC(10,2) NOT NULL,
                            opens_at TIME NOT NULL,
                            closes_at TIME NOT NULL,
                            active BOOLEAN NOT NULL DEFAULT TRUE
);