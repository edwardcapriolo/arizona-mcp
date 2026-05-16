package io.teknek.arizonamcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.util.Map;
import java.util.List;
import java.util.SequencedCollection;

public class OneShotToolCall implements AutoCloseable{

    private HttpClientSseClientTransport transport;
    private McpSyncClient client;

    public OneShotToolCall(String baseUri){
        transport = HttpClientSseClientTransport.builder(baseUri).build();
        client = McpClient.sync(transport).build();
        client.initialize();
        client.ping();
    }

    public String callPrimitive(String toolName, Map<String,Object> arguments){
        McpSchema.CallToolRequest req = new McpSchema.CallToolRequest(toolName, arguments);
        McpSchema.CallToolResult res = call (req);
        return res.toString();
    }

    public McpSchema.CallToolResult call(McpSchema.CallToolRequest request){
        return client.callTool(request);
    }

    public List<McpSchema.Tool> listTools(){
        return client.listTools().tools().stream().toList();
    }

    @Override
    public void close() {
        if (client!=null){
            client.closeGracefully();
        }
    }

}
