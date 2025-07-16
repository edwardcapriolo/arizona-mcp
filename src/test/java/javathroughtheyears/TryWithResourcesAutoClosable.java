package javathroughtheyears;

import org.junit.jupiter.api.Test;

public class TryWithResourcesAutoClosable {
    static class Statement implements AutoCloseable{
        void doIt(){ }
        public void close() throws Exception{ }
    }
    static class Connection implements AutoCloseable {
        Statement createStatement(){
            return new Statement();
        }
        public void close() {
            System.out.println("shut it down");
        }
    }

    @Test
    void theOldDays(){
        Connection connection = null;
        Statement statement = null;
        try {
            connection = new Connection();
            statement = connection.createStatement();
        } finally {
            if (statement != null){
                try {
                    statement.close();
                } catch (Exception ignored) {}
            }
            if (connection != null){
                try {
                    connection.close();
                } catch (Exception ignored) {}
            }
        }
    }

    @Test
    void nowADays(){
        try (Connection conn = new Connection(); Statement statement = conn.createStatement()) {
            statement.doIt();
        } catch (Exception ignored) { }
    }
}
