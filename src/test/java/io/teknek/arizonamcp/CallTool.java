package io.teknek.arizonamcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;

import java.util.Map;


public class CallTool {

    private final McpClientTransport transport;

    public CallTool(McpClientTransport transport) {
        this.transport = transport;
    }

    public void run() {

        var client = McpClient.sync(this.transport).build();

        client.initialize();

        client.ping();

        // List and demonstrate tools
        ListToolsResult toolsList = client.listTools();
        System.out.println("Available Tools = " + toolsList);
        toolsList.tools().stream().forEach(tool -> {
            System.out.println("Tool: " + tool.name() + ", description: " + tool.description() + ", schema: " + tool.inputSchema());
        });

        McpSchema.CallToolResult upperCase = client.callTool( new McpSchema.CallToolRequest("toUpperCase", Map.of("text", "its lower")));
        System.out.println(upperCase);
/*
        CallToolResult weatherForcastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
                Map.of("latitude", "47.6062", "longitude", "-122.3321")));
        System.out.println("Weather Forcast: " + weatherForcastResult);

        CallToolResult alertResult = client.callTool(new CallToolRequest("getAlerts", Map.of("state", "NY")));
        System.out.println("Alert Response = " + alertResult);
*/
        client.closeGracefully();

    }

    public static void main(String [] args){
        var transport = HttpClientSseClientTransport.builder("http://localhost:8080").build();
        new CallTool(transport).run();
    }

}