package jupiter.annotation.meta;

import jupiter.extension.ApiFixtureExtension;
import jupiter.extension.UiExtension;
import jupiter.extension.UserExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({
        UiExtension.class,
        ApiFixtureExtension.class,
        UserExtension.class
})
@Tag("ui")
public @interface UiTest {

}
