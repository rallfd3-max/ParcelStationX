package com.parcelstationx.backup;

public interface BackupDataStore {
  BackupSnapshot snapshot();

  void restore(BackupSnapshot snapshot);
}
