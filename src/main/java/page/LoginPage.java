package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {

    private final SelenideElement emailInput = $("#email");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitButton = $("button[type='submit']");
    private final SelenideElement validationError = $("[data-testid='login-error']");


    private static final String PAGE_TITLE = "Вход";
    private static final String INVALID_EMAIL_OR_PASSWORD = "Неверный email или пароль.";

    @Override
    public LoginPage shouldBeOpened() {
        checkPageTitle(PAGE_TITLE);

        return this;
    }

    @Override
    public String url() {
        return "/app/login";
    }

    public LoginPage shouldHaveEmailInput() {
        emailInput.shouldBe(visible);
        return this;
    }

    public LoginPage shouldHavePasswordInput() {
        passwordInput.shouldBe(visible);
        return this;
    }

    public LoginPage shouldHaveLoginButton() {
        submitButton.shouldBe(visible);
        return this;
    }

    public ProductPage loginAs(String email, String password) {
        setEmail(email);
        setPassword(password);
        return submitValid();
    }

    public LoginPage tryLoginAs(String email, String password) {
        setEmail(email);
        setPassword(password);
        submitButton.click();
        return this;
    }

    public LoginPage shouldShowError() {
        validationError.shouldHave(text(INVALID_EMAIL_OR_PASSWORD));
        return this;
    }

    private ProductPage submitValid() {
        submitButton.click();
        return new ProductPage().shouldBeOpened();
    }

    private LoginPage setEmail(String email) {
        emailInput.setValue(email);
        return this;
    }

    private LoginPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }
}
