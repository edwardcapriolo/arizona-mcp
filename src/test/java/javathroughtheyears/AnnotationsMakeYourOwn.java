package javathroughtheyears;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

public class AnnotationsMakeYourOwn {

    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Idea {
        String type();
    }

    @Test
    @Idea(type = "good")
    public void useJava() throws NoSuchMethodException {
        Idea isIdea = this.getClass()
                .getMethod("useJava", null).getAnnotation(Idea.class);
        Assertions.assertTrue(isIdea.type().equals("good"));
    }
}
