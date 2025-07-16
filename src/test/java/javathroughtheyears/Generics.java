package javathroughtheyears;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class Generics {

    class Something{}
    @Test
    void javaOldDays(){
        List x = new ArrayList();
        x.add(new Something());
        Something s = (Something) x.get(0);
    }

    @Test
    void javaNewDays(){
        List<Something> x = new ArrayList<>();
        x.add(new Something());
        Something s = x.get(0);
    }
}
