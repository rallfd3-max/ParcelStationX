package com.parcelstationx.backup;

import com.parcelstationx.exception.AppException;
import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class BackupService {
  private final BackupDataStore store;

  public BackupService() {
    this.store = null;
  }

  public BackupService(BackupDataStore store) {
    this.store = store;
  }

  public BackupSnapshot backup(Path file) {
    requireStore();
    BackupSnapshot value = store.snapshot();
    write(file, value);
    return value;
  }

  public static String checksum(BackupSnapshot snapshot) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
          ObjectOutputStream output = new ObjectOutputStream(bytes)) {
        output.writeObject(snapshot.customers());
        output.writeObject(snapshot.shelves());
        output.writeObject(snapshot.parcels());
        output.writeObject(snapshot.events());
        output.writeObject(snapshot.exceptions());
        output.writeObject(snapshot.notifications());
        output.writeObject(snapshot.operationLogs());
        output.flush();
        return HexFormat.of().formatHex(digest.digest(bytes.toByteArray()));
      }
    } catch (NoSuchAlgorithmException | IOException exception) {
      throw new AppException("Backup checksum calculation failed.", exception);
    }
  }

  public void restore(Path file) {
    requireStore();
    store.restore(readSnapshot(file));
  }

  public void writeMetadata(Path file, BackupMetadata metadata) {
    write(file, metadata);
  }

  public BackupMetadata readMetadata(Path file) {
    Object value = readObject(file);
    if (value instanceof BackupMetadata metadata) return metadata;
    throw new AppException("Backup file does not contain metadata.");
  }

  private void requireStore() {
    if (store == null) throw new AppException("Backup data store is not configured.");
  }

  private void write(Path file, Object value) {
    try (ObjectOutputStream out =
        new ObjectOutputStream(
            new BufferedOutputStream(java.nio.file.Files.newOutputStream(file)))) {
      out.writeObject(value);
    } catch (IOException e) {
      throw new AppException("Backup write failed.", e);
    }
  }

  private BackupSnapshot readSnapshot(Path file) {
    Object value = readObject(file);
    if (value instanceof BackupSnapshot snapshot) {
      long count =
          snapshot.customers().size()
              + snapshot.shelves().size()
              + snapshot.parcels().size()
              + snapshot.events().size()
              + snapshot.exceptions().size()
              + snapshot.notifications().size()
              + snapshot.operationLogs().size();
      if (count != snapshot.metadata().recordCount()
          || !checksum(snapshot).equals(snapshot.metadata().checksum())) {
        throw new AppException("Backup snapshot checksum is invalid.");
      }
      return snapshot;
    }
    throw new AppException("Backup file does not contain a snapshot.");
  }

  private Object readObject(Path file) {
    try (ObjectInputStream in =
        new ObjectInputStream(new BufferedInputStream(java.nio.file.Files.newInputStream(file)))) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new AppException("Backup file is invalid.", e);
    }
  }
}
