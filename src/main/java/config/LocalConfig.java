package config;

import api.logging.ApiLogMode;

enum LocalConfig implements Config {
    INSTANCE;

    @Override
    public String authServiceUrl() {
        return "http://127.0.0.1:8001";
    }

    @Override
    public String productServiceUrl() {
        return "http://127.0.0.1:8002";
    }

    @Override
    public String adminLogin() {
        return "admin@example.com";
    }

    @Override
    public String adminPassword() {
        return "Admin12345";
    }

    @Override
    public String uiBaseUrl() {
        return "http://127.0.0.1";
    }

    @Override
    public ApiLogMode apiLogMode() {
        return ApiLogMode.valueOf(
                System.getProperty("api.log", "TEST_ONLY").toUpperCase()
        );
    }
}
