package com.parcelstationx.ui.component;

import java.awt.*;
import java.util.Map;
import javax.swing.JPanel;

public final class BarChartPanel extends JPanel {
  private Map<String, Long> values = Map.of();

  public void setValues(Map<String, Long> values) {
    this.values = Map.copyOf(values);
    repaint();
  }

  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    if (values.isEmpty()) return;
    long max = Math.max(1, values.values().stream().mapToLong(Long::longValue).max().orElse(1));
    int width = Math.max(20, getWidth() / values.size());
    int index = 0;
    for (var e : values.entrySet()) {
      int height = (int) Math.round((getHeight() - 40d) * e.getValue() / max);
      graphics.setColor(new Color(36, 123, 160));
      graphics.fillRect(index * width + 8, getHeight() - height - 22, width - 16, height);
      graphics.setColor(Color.DARK_GRAY);
      graphics.drawString(e.getKey(), index * width + 8, getHeight() - 6);
      index++;
    }
  }
}
