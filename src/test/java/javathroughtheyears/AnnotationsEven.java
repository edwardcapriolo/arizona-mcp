package javathroughtheyears;

import org.junit.jupiter.api.Test;

public class AnnotationsEven {

    interface AbcInteface {
        void doIt(int a, int b, long c);
    }
    class AbcImplementation implements AbcInteface{
        @Override
        public void doIt(int a, int b, long c) { }
    }
    @Test
    void abc(){
        //this runs because of annotation discovery
    }
}
