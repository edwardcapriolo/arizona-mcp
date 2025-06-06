package io.teknek.arizonamcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @RequestMapping("/more")
    public String more(){
        PromptTemplate pt = new PromptTemplate("""
                list the tables look for a table that might be joe's table. Then get the columns in that table. 
                """);
        ChatOptions chatOptions = ToolCallingChatOptions.builder()
                //.toolNames("list_tables") Functional bean with name get_columns does not exist in the context.
                //.toolNames("get_columns")
                .build();
        Prompt prompt = pt.create(chatOptions);
        ChatClient.CallResponseSpec callResponse = this.chatClient.prompt(prompt).tools(this).call();
        return callResponse.content();
    }

    @RequestMapping("/table/find/{tableName}")
    public String tableFind(@PathVariable String tableName){
        /*
        PromptTemplate pt = new PromptTemplate(""" 
                list the tables look for a table with a name close to {tableName} then get the columns for that table
                """);*/
        PromptTemplate pt = new PromptTemplate(""" 
                list the tables. Find one table with a name similar to {tableName}. Then get the columns for that table.  
                """);
        Prompt prompt = pt.create(java.util.Map.of("tableName", tableName));
        ChatClient.CallResponseSpec callResponse = chatClient.prompt(prompt).tools(this).call();
        return callResponse.content();
    }

    @Tool(description = "list the tables in the database", name = "list_tables")
    public List<String> findDatabases(){
        return List.of("eds_table", "joes_table");
    }

    @Tool(description = "get columns in table", name = "get_columns")
    public List<String> getColumnsInTable(String tableName){
        if (tableName.equalsIgnoreCase("eds_table")){
            return List.of("id", "name");
        } if (tableName.equalsIgnoreCase("joes_table")){
            return List.of("id", "color");
        }
        return null;
    }



}
