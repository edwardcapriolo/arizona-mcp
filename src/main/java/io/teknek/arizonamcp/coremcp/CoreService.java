package io.teknek.arizonamcp.coremcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class CoreService {

    @Tool(description = "Say hello", name = "core_say_hello")
    public String sayHello() {
        return "Hello World!";
    }

    @Tool(description = "Say hello to a specific person", name = "core_say_hello_to_someone")
    public String sayHelloToMe(String person) {
        return "Hello " + person;
    }

    @Tool(description = "Get the current time in milliseconds from epoch for the system clock", name = "core_current_time_in_milliseconds")
    public Long currentTimeMilliseconds() {
        return System.currentTimeMillis();
    }

    @Tool(description = "streaming update", name ="core_streaming_update" )
    public Flux<String> stream(){
        return Flux.interval(Duration.ofSeconds(1)) // Emit every 1s
                .map(tick -> "String " + tick) // Map to string
                .take(10);
    }

    /*
    InvocationRequest request = new DefaultInvocationRequest();
request.setPomFile(new File("path/to/pom.xml"));
request.setGoals(Collections.singletonList("test"));

Invoker invoker = new DefaultInvoker();
invoker.execute(request);
     */


    @Bean
    ToolCallbackProvider coreTools(){
        return MethodToolCallbackProvider.builder().toolObjects(this).build();
    }
}
