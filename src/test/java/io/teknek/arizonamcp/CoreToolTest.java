package io.teknek.arizonamcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CoreToolTest {
    @LocalServerPort
    private int localServerPort;
    @Test
    public void testHello(){
        var transport = HttpClientSseClientTransport.builder("http://localhost:"+localServerPort+"/").build();
        try (var client = McpClient.sync(transport).build()){
            client.initialize();
            client.ping();
            List<String> tools = client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
            Assertions.assertTrue(tools.contains("core_current_time_in_milliseconds"));
            McpSchema.CallToolResult res = client.callTool( new McpSchema.CallToolRequest("core_say_hello_to_someone",
                    Map.of("person", "bob")));
            //System.out.println(res);
            Assertions.assertTrue(res.content().toString().contains("Hello bob"), res.content().toString() );
            client.closeGracefully();
        }
    }
}
