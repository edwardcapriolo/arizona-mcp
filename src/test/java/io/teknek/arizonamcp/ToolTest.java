package io.teknek.arizonamcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SpringBootTest(
        //webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = McpServerApplication.class
)
public class ToolTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int localServerPort;

    @Test
    public void callTool() throws InterruptedException {

        var transport = HttpClientSseClientTransport.builder("http://localhost:" + localServerPort).build();
        try(var client = McpClient.sync(transport).build()) {
            client.initialize();
            client.ping();
            java.util.List<String> tools = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
            Assertions.assertEquals(List.of("toUpperCase"), tools);
            client.closeGracefully();
        }
    }


    @Test
    void testTableFind() {
        ResponseEntity<String> response = restTemplate.getForEntity("/table/find/ed", String.class);
        Assertions.assertTrue(response.getBody().contains("eds_table"));
        //Actual message like:
        /*
        The tables with a name similar to "ed" are:
1. ed_table
2. joes_table

These two table names are not provided in the response. If you need more information, please let me know.
         */
    }

    @Test
    void testEntirePromptWithBothTools() {
        ResponseEntity<String> response = restTemplate.getForEntity("/table/find/ed", String.class);
        Assertions.assertTrue(response.getBody().contains("eds_table"));
        //Actual message like:
        /*
        The tables with a name similar to "ed" are:
1. ed_table
2. joes_table

These two table names are not provided in the response. If you need more information, please let me know.
         */
    }



    @Disabled
    public void zzzGoSlow() throws InterruptedException {
        System.out.println("Im here at http://localhost:" + localServerPort + "/ok");
        Thread.sleep(5* 120_000);
    }
}
