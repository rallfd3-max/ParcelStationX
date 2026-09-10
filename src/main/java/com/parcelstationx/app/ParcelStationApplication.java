package com.parcelstationx.app;

import com.parcelstationx.backup.BackupService;
import com.parcelstationx.backup.JdbcBackupDataStore;
import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.dao.impl.CustomerDaoImpl;
import com.parcelstationx.dao.impl.ExceptionRecordDaoImpl;
import com.parcelstationx.dao.impl.NotificationRecordDaoImpl;
import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.ParcelEventDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.UserDaoImpl;
import com.parcelstationx.exception.AppException;
import com.parcelstationx.service.AuthenticationService;
import com.parcelstationx.service.CustomerService;
import com.parcelstationx.service.ExceptionService;
import com.parcelstationx.service.NotificationService;
import com.parcelstationx.service.ParcelService;
import com.parcelstationx.service.PasswordHasher;
import com.parcelstationx.service.ShelfService;
import com.parcelstationx.service.StatisticsService;
import com.parcelstationx.service.TransactionRunner;
import com.parcelstationx.service.UserService;
import com.parcelstationx.task.NotificationQueue;
import com.parcelstationx.ui.frame.LoginFrame;
import com.parcelstationx.ui.frame.MainFrame;
import com.parcelstationx.ui.panel.BackupPanel;
import com.parcelstationx.ui.panel.CustomerPanel;
import com.parcelstationx.ui.panel.DashboardPanel;
import com.parcelstationx.ui.panel.ExceptionPanel;
import com.parcelstationx.ui.panel.InboundPanel;
import com.parcelstationx.ui.panel.InventoryPanel;
import com.parcelstationx.ui.panel.OperationLogPanel;
import com.parcelstationx.ui.panel.OutboundPanel;
import com.parcelstationx.ui.panel.ShelfPanel;
import com.parcelstationx.ui.panel.StatisticsPanel;
import com.parcelstationx.ui.panel.UserPanel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class ParcelStationApplication {
  private ParcelStationApplication() {}

  public static void main(String[] args) {
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory(AppConfig.loadDatabaseConfig());
      AuthenticationService authentication =
          new AuthenticationService(new UserDaoImpl(connectionFactory), new PasswordHasher());
      CustomerDaoImpl customers = new CustomerDaoImpl(connectionFactory);
      ShelfDaoImpl shelves = new ShelfDaoImpl(connectionFactory);
      ParcelDaoImpl parcels = new ParcelDaoImpl(connectionFactory);
      ParcelEventDaoImpl events = new ParcelEventDaoImpl(connectionFactory);
      OperationLogDaoImpl logs = new OperationLogDaoImpl(connectionFactory);
      ExceptionRecordDaoImpl exceptionRecords = new ExceptionRecordDaoImpl(connectionFactory);
      NotificationRecordDaoImpl notificationRecords =
          new NotificationRecordDaoImpl(connectionFactory);
      TransactionRunner transactions = new TransactionRunner(connectionFactory);
      NotificationService notifications =
          new NotificationService(notificationRecords, customers, new NotificationQueue());
      ParcelService parcelService =
          new ParcelService(transactions, customers, shelves, parcels, events, logs)
              .withNotifications(notifications);
      ExceptionService exceptionService =
          new ExceptionService(transactions, exceptionRecords, parcels, events, logs);
      StatisticsService statistics = new StatisticsService(parcels, shelves, exceptionRecords);
      BackupService backups =
          new BackupService(
              new JdbcBackupDataStore(
                  transactions,
                  customers,
                  shelves,
                  parcels,
                  events,
                  exceptionRecords,
                  notificationRecords,
                  logs));
      SwingUtilities.invokeLater(
          () ->
              new LoginFrame(
                      (username, password) -> {
                        try {
                          var user = authentication.login(username, password);
                          var pages = new LinkedHashMap<String, javax.swing.JComponent>();
                          pages.put("首页", new DashboardPanel(statistics));
                          pages.put("快件入库", new InboundPanel(parcelService, user.id()));
                          pages.put("在库快件", new InventoryPanel(parcels));
                          pages.put("快件出库", new OutboundPanel(parcelService, user.id()));
                          pages.put("客户管理", new CustomerPanel(new CustomerService(customers)));
                          pages.put("货架管理", new ShelfPanel(new ShelfService(shelves)));
                          pages.put(
                              "员工管理",
                              new UserPanel(
                                  new UserService(
                                      new UserDaoImpl(connectionFactory), new PasswordHasher())));
                          pages.put("操作日志", new OperationLogPanel(logs));
                          pages.put("异常件", new ExceptionPanel(exceptionService, user.id()));
                          pages.put("统计报表", new StatisticsPanel(statistics));
                          pages.put("备份恢复", new BackupPanel(backups));
                          MainFrame frame = new MainFrame(user, pages);
                          frame.addWindowListener(
                              new WindowAdapter() {
                                @Override
                                public void windowClosing(WindowEvent event) {
                                  notifications.close();
                                }
                              });
                          frame.setVisible(true);
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
