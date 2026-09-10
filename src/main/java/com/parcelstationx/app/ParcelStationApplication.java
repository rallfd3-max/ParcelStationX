package com.parcelstationx.app;

import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.dao.impl.UserDaoImpl;
import com.parcelstationx.exception.AppException;
import com.parcelstationx.service.AuthenticationService;
import com.parcelstationx.service.PasswordHasher;
import com.parcelstationx.ui.frame.LoginFrame;
import com.parcelstationx.ui.frame.MainFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class ParcelStationApplication {
  private ParcelStationApplication() {}

  public static void main(String[] args) {
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory(AppConfig.loadDatabaseConfig());
      AuthenticationService authentication =
          new AuthenticationService(new UserDaoImpl(connectionFactory), new PasswordHasher());
      SwingUtilities.invokeLater(
          () ->
              new LoginFrame(
                      (username, password) -> {
                        try {
                          authentication.login(username, password);
                          new MainFrame().setVisible(true);
                        } catch (AppException exception) {
                          JOptionPane.showMessageDialog(
                              null, exception.getMessage(), "登录失败", JOptionPane.WARNING_MESSAGE);
                        }
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
