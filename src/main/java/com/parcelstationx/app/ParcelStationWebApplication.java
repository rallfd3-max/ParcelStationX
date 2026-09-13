package com.parcelstationx.app;

import com.parcelstationx.api.auth.SessionManager;
import com.parcelstationx.api.http.ApiServer;
import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.dao.impl.ParcelDaoImpl;
import com.parcelstationx.dao.impl.UserDaoImpl;
import com.parcelstationx.exception.AppException;
import com.parcelstationx.service.AuthenticationService;
import com.parcelstationx.service.PasswordHasher;
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
      var server =
          new ApiServer(
              new InetSocketAddress("127.0.0.1", port),
              authentication,
              new ParcelDaoImpl(connections),
              new SessionManager());
      Runtime.getRuntime().addShutdownHook(new Thread(server::close, "parcel-api-shutdown"));
      server.start();
      System.out.println("ParcelStationX Web API listening on http://127.0.0.1:" + server.port());
    } catch (AppException | IOException | NumberFormatException exception) {
      System.err.println("Unable to start ParcelStationX Web API: " + exception.getMessage());
    }
  }
}
