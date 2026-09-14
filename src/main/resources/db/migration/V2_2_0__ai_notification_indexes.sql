-- V2.2 non-destructive indexes for staged reminder dedupe and overdue scans.
CREATE INDEX idx_notification_parcel_type ON notification_records(parcel_id, notification_type);
CREATE INDEX idx_notification_status_created ON notification_records(status, created_at);
CREATE INDEX idx_parcel_status_arrived ON parcels(status, arrived_at);
