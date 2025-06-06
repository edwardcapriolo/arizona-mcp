package io.teknek.arizonamcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    @RequestMapping("/ok")
    public String ok(){
        return "ok";
    }

    @RequestMapping("/go")
    public String go(){
        PromptTemplate pt = new PromptTemplate("""
                Tell me a funny dad joke.
                """);
        Prompt p = pt.create();
        return this.chatClient.prompt(p).call().content();
    }
}
