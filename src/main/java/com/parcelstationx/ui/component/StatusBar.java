package com.parcelstationx.ui.component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

public final class StatusBar extends JLabel {
  public StatusBar() {
    super("就绪");
    setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
  }

  public void showMessage(String message) {
    setText(message == null ? "就绪" : message);
  }
}
