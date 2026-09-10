package com.parcelstationx.backup;
import java.io.Serializable; import java.time.LocalDateTime;
public record BackupMetadata(String version, LocalDateTime createdAt, long recordCount, String checksum) implements Serializable { private static final long serialVersionUID=1L; }
