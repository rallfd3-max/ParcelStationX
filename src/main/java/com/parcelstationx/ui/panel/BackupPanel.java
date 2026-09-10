package com.parcelstationx.ui.panel;

import com.parcelstationx.backup.BackupService;
import java.awt.GridLayout;
import java.nio.file.Path;
import javax.swing.*;

public final class BackupPanel extends JPanel {
  public BackupPanel(BackupService service) {
    setLayout(new GridLayout(0, 1, 8, 8));
    JTextField file = new JTextField("backup-data/parcelstationx.ser");
    JButton backup = new JButton("创建备份"), restore = new JButton("恢复备份");
    add(file);
    add(backup);
    add(restore);
    backup.addActionListener(e -> run(() -> service.backup(Path.of(file.getText())), "备份完成"));
    restore.addActionListener(
        e -> {
          if (JOptionPane.showConfirmDialog(this, "恢复会替换现有业务数据，确认继续？") == JOptionPane.YES_OPTION)
            run(
                () -> {
                  service.restore(Path.of(file.getText()));
                  return null;
                },
                "恢复完成");
        });
  }

  private void run(java.util.concurrent.Callable<?> action, String message) {
    new SwingWorker<Object, Void>() {
      protected Object doInBackground() throws Exception {
        return action.call();
      }

      protected void done() {
        try {
          get();
          JOptionPane.showMessageDialog(BackupPanel.this, message);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(BackupPanel.this, "操作失败：" + e.getCause().getMessage());
        }
      }
    }.execute();
  }
}
