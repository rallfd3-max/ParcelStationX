package com.parcelstationx.ui.frame;

import java.awt.*;
import javax.swing.*;

public final class LoginFrame extends JFrame {
  public interface LoginHandler {
    void login(String username, char[] password);
  }

  public LoginFrame(LoginHandler onLogin) {
    super("ParcelStationX 登录");
    JTextField username = new JTextField(16);
    JPasswordField password = new JPasswordField(16);
    JButton login = new JButton("登录");
    login.addActionListener(event -> onLogin.login(username.getText(), password.getPassword()));
    JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
    panel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    panel.add(new JLabel("用户名"));
    panel.add(username);
    panel.add(new JLabel("密码"));
    panel.add(password);
    panel.add(new JLabel());
    panel.add(login);
    setContentPane(panel);
    pack();
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
}
