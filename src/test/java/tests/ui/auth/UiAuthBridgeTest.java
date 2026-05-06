package tests.ui.auth;

import auth.UiAuthBridge;
import jupiter.annotation.CurrentUser;
import jupiter.annotation.TestUser;
import jupiter.annotation.meta.UiTest;
import model.auth.common.AuthContext;
import org.junit.jupiter.api.Test;
import page.ProductPage;


@UiTest
public class UiAuthBridgeTest {
    @TestUser
    @Test
    void shouldOpenProductsWithApiAuthenticatedUser(@CurrentUser AuthContext user) {
        new UiAuthBridge()
                .authenticateAs(user);

        new ProductPage()
                .open()
                .shouldBeOpened();
    }
}
