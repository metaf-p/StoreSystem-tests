package tests.ui.auth;

import jupiter.annotation.meta.UiTest;
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
