package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class RegistrationPage extends BasePage<RegistrationPage> {
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement emailInput = $("#email");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitButton = $("button[type='submit']");

    private static final String PAGE_TITLE = "Регистрация";

    @Override
    public RegistrationPage shouldBeOpened() {
        checkPageTitle(PAGE_TITLE);
        return this;
    }

    @Override
    public String url() {
        return "/app/register";
    }

    public RegistrationPage shouldHaveNameInput() {
        usernameInput.shouldBe(visible);
        return this;
    }

    public RegistrationPage shouldHaveEmailInput() {
        emailInput.shouldBe(visible);
        return this;
    }

    public RegistrationPage shouldHavePasswordInput() {
        passwordInput.shouldBe(visible);
        return this;
    }

    public RegistrationPage shouldHaveRegisterButton() {
        submitButton.shouldBe(visible);
        return this;
    }

}
