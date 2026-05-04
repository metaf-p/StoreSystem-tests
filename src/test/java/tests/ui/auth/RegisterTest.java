package tests.ui.auth;

import jupiter.annotation.UiTest;
import org.junit.jupiter.api.Test;
import page.RegistrationPage;

@UiTest
public class RegisterTest {
    @Test
    void shouldDisplayRegisterPage() {
        new RegistrationPage()
                .open()
                .shouldBeOpened()
                .shouldHaveNameInput()
                .shouldHaveEmailInput()
                .shouldHavePasswordInput()
                .shouldHaveRegisterButton();
    }
}
