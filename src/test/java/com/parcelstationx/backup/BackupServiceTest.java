package com.parcelstationx.backup;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.exception.AppException;
import java.nio.file.*;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BackupServiceTest {
  @Test
  void writesAndReadsMetadata() throws Exception {
    Path file = Files.createTempFile("backup", ".ser");
    BackupMetadata value = new BackupMetadata("1", LocalDateTime.now(), 3, "abc");
    BackupService service = new BackupService();
    service.writeMetadata(file, value);
    assertEquals(value, service.readMetadata(file));
    Files.delete(file);
  }

  @Test
  void rejectsCorruptFile() throws Exception {
    Path file = Files.createTempFile("backup", ".ser");
    Files.writeString(file, "invalid");
    assertThrows(AppException.class, () -> new BackupService().readMetadata(file));
    Files.delete(file);
  }
}
