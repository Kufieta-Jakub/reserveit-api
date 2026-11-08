CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE reservations
    ADD CONSTRAINT no_overlapping_reservations
    EXCLUDE USING gist (
        facility_id WITH =,
        tsrange(start_time, end_time) WITH &&
    )
    WHERE (status <> 'CANCELLED');