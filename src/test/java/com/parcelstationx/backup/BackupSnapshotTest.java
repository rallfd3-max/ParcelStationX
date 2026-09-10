package com.parcelstationx.backup;

import static org.junit.jupiter.api.Assertions.*;

import com.parcelstationx.model.Customer;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BackupSnapshotTest {
  @Test
  void serializesBusinessDataAndRestoresIt() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    BackupSnapshot snapshot =
        new BackupSnapshot(
            new BackupMetadata("1", now, 1, "1"),
            List.of(new Customer(1L, "A", "13800000000", null, null, null, now, now)),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    class Store implements BackupDataStore {
      BackupSnapshot value = snapshot;

      public BackupSnapshot snapshot() {
        return value;
      }

      public void restore(BackupSnapshot restored) {
        value = restored;
      }
    }
    Store store = new Store();
    BackupService service = new BackupService(store);
    var file = Files.createTempFile("snapshot", ".ser");
    service.backup(file);
    store.value = null;
    service.restore(file);
    assertEquals("A", store.value.customers().get(0).name());
    Files.delete(file);
  }

  @Test
  void rejectsSnapshotWithInvalidChecksum() throws Exception {
    BackupSnapshot invalid =
        new BackupSnapshot(
            new BackupMetadata("1", LocalDateTime.now(), 99, "bad"),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of());
    class Store implements BackupDataStore {
      public BackupSnapshot snapshot() {
        return invalid;
      }

      public void restore(BackupSnapshot value) {}
    }
    BackupService writer = new BackupService(new Store());
    var file = Files.createTempFile("invalid-snapshot", ".ser");
    writer.backup(file);
    assertThrows(com.parcelstationx.exception.AppException.class, () -> writer.restore(file));
    Files.delete(file);
  }
}
