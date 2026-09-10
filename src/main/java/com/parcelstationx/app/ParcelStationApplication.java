package com.parcelstationx.app;

import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.exception.AppException;
import com.parcelstationx.ui.frame.LoginFrame;
import com.parcelstationx.ui.frame.MainFrame;
import javax.swing.SwingUtilities;

public final class ParcelStationApplication {
  private ParcelStationApplication() {}

  public static void main(String[] args) {
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory(AppConfig.loadDatabaseConfig());
      SwingUtilities.invokeLater(
          () ->
              new LoginFrame(
                      () -> {
                        MainFrame frame = new MainFrame();
                        frame.setVisible(true);
                      })
                  .setVisible(true));
      System.out.println(
          "ParcelStationX configuration loaded. JDBC connection factory is ready: "
              + connectionFactory.getClass().getSimpleName());
    } catch (AppException exception) {
      System.err.println(exception.getMessage());
    }
  }
}
