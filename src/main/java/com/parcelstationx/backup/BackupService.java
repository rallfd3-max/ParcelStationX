package com.parcelstationx.backup;

import com.parcelstationx.exception.AppException;
import java.io.*;
import java.nio.file.Path;

public final class BackupService {
  public void writeMetadata(Path file, BackupMetadata metadata) {
    try (ObjectOutputStream out =
        new ObjectOutputStream(
            new BufferedOutputStream(java.nio.file.Files.newOutputStream(file)))) {
      out.writeObject(metadata);
    } catch (IOException e) {
      throw new AppException("Backup metadata write failed.", e);
    }
  }

  public BackupMetadata readMetadata(Path file) {
    try (ObjectInputStream in =
        new ObjectInputStream(new BufferedInputStream(java.nio.file.Files.newInputStream(file)))) {
      return (BackupMetadata) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new AppException("Backup file is invalid.", e);
    }
  }
}
