-- ParcelStationX V2 forward migration. Run after the V1 schema and before the V2 seed.
CREATE TABLE IF NOT EXISTS shelf_layout (
    shelf_id BIGINT PRIMARY KEY,
    position_x DECIMAL(10,3) NOT NULL, position_y DECIMAL(10,3) NOT NULL, position_z DECIMAL(10,3) NOT NULL,
    rotation_y DECIMAL(10,6) NOT NULL DEFAULT 0,
    width DECIMAL(10,3) NOT NULL, height DECIMAL(10,3) NOT NULL, depth DECIMAL(10,3) NOT NULL,
    columns_count INT NOT NULL, levels_count INT NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_layout_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CONSTRAINT chk_layout_dimensions CHECK (width > 0 AND height > 0 AND depth > 0),
    CONSTRAINT chk_layout_grid CHECK (columns_count > 0 AND levels_count > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shelf_slots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, shelf_id BIGINT NOT NULL,
    slot_code VARCHAR(50) NOT NULL UNIQUE, level_index INT NOT NULL, column_index INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME NOT NULL,
    CONSTRAINT fk_slot_shelf FOREIGN KEY (shelf_id) REFERENCES shelves(id),
    CONSTRAINT uq_slot_grid UNIQUE (shelf_id, level_index, column_index),
    CONSTRAINT chk_slot_grid CHECK (level_index >= 0 AND column_index >= 0)
) ENGINE=InnoDB;

ALTER TABLE parcels ADD COLUMN slot_id BIGINT NULL;
ALTER TABLE parcels ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE parcels ADD CONSTRAINT fk_parcel_slot FOREIGN KEY (slot_id) REFERENCES shelf_slots(id);
ALTER TABLE parcels ADD CONSTRAINT uq_parcel_active_slot UNIQUE (slot_id);

CREATE TABLE IF NOT EXISTS parcel_relocations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, parcel_id BIGINT NOT NULL,
    from_slot_id BIGINT NULL, new_slot_id BIGINT NULL, operator_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL, created_at DATETIME NOT NULL,
    CONSTRAINT fk_relocation_parcel FOREIGN KEY (parcel_id) REFERENCES parcels(id),
    CONSTRAINT fk_relocation_from_slot FOREIGN KEY (from_slot_id) REFERENCES shelf_slots(id),
    CONSTRAINT fk_relocation_new_slot FOREIGN KEY (new_slot_id) REFERENCES shelf_slots(id),
    CONSTRAINT fk_relocation_operator FOREIGN KEY (operator_id) REFERENCES users(id),
    INDEX idx_relocation_parcel_time (parcel_id, created_at)
) ENGINE=InnoDB;
