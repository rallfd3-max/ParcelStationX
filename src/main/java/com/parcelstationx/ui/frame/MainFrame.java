package com.parcelstationx.ui.frame;

import com.parcelstationx.model.User;
import com.parcelstationx.model.UserRole;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public final class MainFrame extends JFrame {
  private final CardLayout cards = new CardLayout();
  private final JPanel content = new JPanel(cards);

  public MainFrame(User user, Map<String, JComponent> pages) {
    super("ParcelStationX - 当前用户：" + user.displayName());
    JPanel navigation = new JPanel(new GridLayout(0, 1, 4, 4));
    for (var entry : visiblePages(user, pages).entrySet()) {
      content.add(entry.getValue(), entry.getKey());
      JButton button = new JButton(entry.getKey());
      button.addActionListener(event -> cards.show(content, entry.getKey()));
      navigation.add(button);
    }
    add(navigation, BorderLayout.WEST);
    add(content, BorderLayout.CENTER);
    setSize(1100, 700);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }

  private Map<String, JComponent> visiblePages(User user, Map<String, JComponent> pages) {
    Map<String, JComponent> visible = new LinkedHashMap<>(pages);
    if (user.role() != UserRole.ADMIN) {
      visible.remove("员工管理");
      visible.remove("备份恢复");
    }
    return visible;
  }
}
