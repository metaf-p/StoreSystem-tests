package page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ProductPage extends BasePage<ProductPage> {
    private final SelenideElement logoutLink = $("[data-testid='logout']");

    private static final String PAGE_TITLE = "Управление продуктами";


    @Override
    public ProductPage shouldBeOpened() {
        checkPageTitle(PAGE_TITLE);
        return this;
    }

    @Override
    public String url() {
        return "/products";
    }

    public LoginPage logout() {
        logoutLink.click();
        return new LoginPage().shouldBeOpened();
    }
}
