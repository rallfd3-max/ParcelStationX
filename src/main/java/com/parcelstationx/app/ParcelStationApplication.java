package com.parcelstationx.app;

import com.parcelstationx.config.AppConfig;
import com.parcelstationx.config.ConnectionFactory;
import com.parcelstationx.exception.AppException;

public final class ParcelStationApplication {
    private ParcelStationApplication() {
    }

    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory(AppConfig.loadDatabaseConfig());
            System.out.println("ParcelStationX configuration loaded. JDBC connection factory is ready: "
                    + connectionFactory.getClass().getSimpleName());
        } catch (AppException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
