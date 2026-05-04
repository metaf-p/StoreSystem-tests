package tests.ui.auth;

import data.auth.AuthTestData;
import data.auth.AuthUserFixture;
import data.auth.UserCleanup;
import jupiter.annotation.UiTest;
import model.auth.request.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import page.LoginPage;
import page.ProductPage;

@UiTest
public class LoginTest {

    @Test
    void shouldLoadLoginPage() {
        new LoginPage()
                .open()
                .shouldBeOpened()
                .shouldHaveEmailInput()
                .shouldHavePasswordInput()
                .shouldHaveLoginButton();
    }

    @Test
    void shouldLoginWithValidUserData(
            AuthUserFixture authUserFixture,
            UserCleanup userCleanup
    ) {

        RegisterUserRequest registerUserRequest = AuthTestData.uniqueUser();
        authUserFixture.registerUser(registerUserRequest, userCleanup);

        new LoginPage()
                .open()
                .shouldBeOpened()
                .loginAs(registerUserRequest.email(), registerUserRequest.password());
    }

    @Test
    void shouldNotLoginWithInvalidUserData() {
        RegisterUserRequest notRegisteredUser = AuthTestData.uniqueUser();

        new LoginPage()
                .open()
                .shouldBeOpened()
                .tryLoginAs(notRegisteredUser.email(), notRegisteredUser.password())
                .shouldShowError()
                .shouldBeOpened();
    }

    @Test
    void shouldLogoutAndOpenLogin(
            AuthUserFixture authUserFixture,
            UserCleanup userCleanup
    ) {

        RegisterUserRequest registerUserRequest = AuthTestData.uniqueUser();
        authUserFixture.registerUser(registerUserRequest, userCleanup);

        LoginPage loginPage = new LoginPage()
                .open()
                .shouldBeOpened()
                .loginAs(registerUserRequest.email(), registerUserRequest.password())
                .shouldBeOpened()
                .logout();

        new ProductPage()
                .open();

        loginPage.shouldBeOpened();
    }
}
