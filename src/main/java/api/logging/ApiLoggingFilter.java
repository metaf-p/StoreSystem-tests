package api.logging;

import config.Config;
import io.restassured.filter.Filter;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import java.util.List;

public final class ApiLoggingFilter {
    private ApiLoggingFilter() {
    }

    public static List<Filter> filters() {
        if (Config.getInstance().apiLogMode() == ApiLogMode.OFF) {
            return List.of();
        }

        return List.of(
                new RequestLoggingFilter(LogDetail.ALL),
                new ResponseLoggingFilter(LogDetail.ALL)
        );
    }
}
