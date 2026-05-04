package config;

import api.logging.ApiLogMode;

public interface Config {

    static Config getInstance() {
        return LocalConfig.INSTANCE;
    }

    String authServiceUrl();

    String productServiceUrl();

    String adminLogin();

    String adminPassword();

    String uiBaseUrl();

    ApiLogMode apiLogMode();
}
