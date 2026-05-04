package page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends BasePage<?>> {
    protected final SelenideElement pageTitle = $("h1");

    public abstract T shouldBeOpened();

    public abstract String url();

    @SuppressWarnings("unchecked")
    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    protected void checkPageTitle(String title) {
        pageTitle.shouldHave(text(title));
    }
}