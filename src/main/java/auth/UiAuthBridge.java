package auth;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import model.auth.common.AuthContext;
import org.openqa.selenium.Cookie;

public class UiAuthBridge {

    public UiAuthBridge authenticateAs(AuthContext user) {
        Selenide.open("/");
        WebDriverRunner.getWebDriver().manage()
                .addCookie(new Cookie("refresh_token", user.refreshToken()));
        return this;
    }
}
