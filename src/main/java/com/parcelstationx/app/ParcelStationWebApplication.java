package com.parcelstationx.app;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.http.ApiServer;
import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.dao.impl.CustomerDaoImpl;
import com.parcelstationx.dao.impl.ExceptionRecordDaoImpl;
import com.parcelstationx.dao.impl.OperationLogDaoImpl;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.ParcelEventDaoImpl;
import com.parcelstationx.dao.impl.ParcelRelocationDaoImpl;
import com.parcelstationx.dao.impl.ShelfDaoImpl;
import com.parcelstationx.dao.impl.ShelfLayoutDaoImpl;
import com.parcelstationx.dao.impl.ShelfSlotDaoImpl;
import com.parcelstationx.dao.impl.UserDaoImpl;
import com.parcelstationx.exception.AppException;
import com.parcelstationx.service.AuthenticationService;
import com.parcelstationx.service.DashboardAnalyticsService;
import com.parcelstationx.service.ExceptionService;
import com.parcelstationx.service.ParcelQueryService;
import com.parcelstationx.service.ParcelService;
import com.parcelstationx.service.PasswordHasher;
import com.parcelstationx.service.RelocationService;
import com.parcelstationx.service.TransactionRunner;
import com.parcelstationx.service.UserService;
import com.parcelstationx.service.WarehouseLayoutService;
import java.io.IOException;
import java.net.InetSocketAddress;

public final class ParcelStationWebApplication {
  private ParcelStationWebApplication() {}

  public static void main(String[] args) {
    int port = Integer.parseInt(System.getenv().getOrDefault("PARCEL_HTTP_PORT", "8080"));
    try {
      var connections = new ConnectionFactory(AppConfig.loadDatabaseConfig());
      var authentication =
          new AuthenticationService(new UserDaoImpl(connections), new PasswordHasher());
      var users = new UserDaoImpl(connections);
      var parcels = new ParcelDaoImpl(connections);
      var shelves = new ShelfDaoImpl(connections);
      var layouts = new ShelfLayoutDaoImpl(connections);
      var slots = new ShelfSlotDaoImpl(connections);
      var relocations = new ParcelRelocationDaoImpl(connections);
      var events = new ParcelEventDaoImpl(connections);
      var logs = new OperationLogDaoImpl(connections);
      var transaction = new TransactionRunner(connections);
      var warehouse = new WarehouseLayoutService(shelves, layouts, slots, parcels);
      var customers = new CustomerDaoImpl(connections);
      var parcelService = new ParcelService(transaction, customers, shelves, parcels, events, logs);
      var relocationService =
          new RelocationService(transaction, parcels, slots, shelves, relocations, events, logs);
      var server =
          new ApiServer(
              new InetSocketAddress("127.0.0.1", port),
              authentication,
              parcels,
              new SessionManager(),
              warehouse,
              relocationService,
              relocations,
              new ParcelQueryService(
                  parcels, customers, users, shelves, slots, events, relocations),
              new ExceptionService(
                  transaction, new ExceptionRecordDaoImpl(connections), parcels, events, logs),
              new UserService(users, new PasswordHasher()),
              parcelService,
              new DashboardAnalyticsService(warehouse, new ExceptionRecordDaoImpl(connections), logs));
      Runtime.getRuntime().addShutdownHook(new Thread(server::close, "parcel-api-shutdown"));
      server.start();
      System.out.println("ParcelStationX Web API listening on http://127.0.0.1:" + server.port());
    } catch (AppException | IOException | NumberFormatException exception) {
      System.err.println("Unable to start ParcelStationX Web API: " + exception.getMessage());
    }
  }
}
