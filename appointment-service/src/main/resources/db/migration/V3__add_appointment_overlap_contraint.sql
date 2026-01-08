-- Required for EXCLUDE constraint support
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Prevent overlapping appointments per patient
ALTER TABLE appointment
ADD CONSTRAINT no_overlap_per_patient
EXCLUDE USING gist (
  patient_id WITH =,
  tsrange(start_time, end_time) WITH &&
);
ALTER TABLE appointment
ALTER COLUMN start_time TYPE TIMESTAMP
WITHOUT TIME ZONE,
ALTER COLUMN end_time TYPE TIMESTAMP
WITHOUT TIME ZONE;
