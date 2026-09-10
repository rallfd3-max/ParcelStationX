package com.parcelstationx.ui.component;

import java.awt.BorderLayout;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class SearchBar extends JPanel {
  public SearchBar(String hint, Consumer<String> search) {
    setLayout(new BorderLayout(6, 0));
    JTextField field = new JTextField(hint, 20);
    JButton button = new JButton("查询");
    button.addActionListener(event -> search.accept(field.getText().trim()));
    add(field, BorderLayout.CENTER);
    add(button, BorderLayout.EAST);
  }
}
