package com.parcelstationx.ui.component;

import java.awt.FlowLayout;
import java.util.function.IntConsumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class PaginationPanel extends JPanel {
  private int page = 1;
  private final JLabel label = new JLabel("第 1 页");

  public PaginationPanel(IntConsumer listener) {
    super(new FlowLayout(FlowLayout.RIGHT));
    JButton previous = new JButton("上一页");
    JButton next = new JButton("下一页");
    previous.addActionListener(event -> change(Math.max(1, page - 1), listener));
    next.addActionListener(event -> change(page + 1, listener));
    add(previous);
    add(label);
    add(next);
  }

  private void change(int value, IntConsumer listener) {
    page = value;
    label.setText("第 " + page + " 页");
    listener.accept(page);
  }
}
