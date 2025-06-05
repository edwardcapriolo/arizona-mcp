package io.teknek.arizonamcp;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class ToolTest {

    @LocalServerPort
    private int localServerPort;

    @Test
    public void callTool(){
        var transport = HttpClientSseClientTransport.builder("http://localhost:" + localServerPort).build();
        new CallTool(transport).run();
    }
}
