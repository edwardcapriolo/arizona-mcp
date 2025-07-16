package javathroughtheyears;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static javathroughtheyears.StaticImports.DslHelper.divide;
import static javathroughtheyears.StaticImports.DslHelper.multiply;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaticImports {

    @Test
    void oldWay(){
        Assertions.assertEquals(5, 5);
    }

    @Test
    void newWay(){
        assertEquals(5, 5);
    }

    static class DslHelper{
        static int divide(int x, int y){
            return x/y;
        }
        static int multiply(int x, int y){
            return x * y;
        }
    }

    @Test
    void soFunctional(){
        assertEquals(10,  divide(multiply(4, 5), 2) );
    }
}
