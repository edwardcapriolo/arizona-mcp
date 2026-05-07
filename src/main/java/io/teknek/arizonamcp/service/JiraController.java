package io.teknek.arizonamcp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RestController
public class JiraController {

    private final ChatClient chatClient;
    private final JiraService jiraService;

    public JiraController(ChatClient.Builder chatClientBuilder, JiraService jiraService){
        this.chatClient = chatClientBuilder.build();
        this.jiraService = jiraService;
    }

    @RequestMapping("list_comments")
    public List<JiraService.SerializedComment> listComments(@RequestParam String issue){
        return jiraService.findCommentsForIssue(issue);
    }

    @RequestMapping("/issues_for_me")
    public List<JiraService.SerializedComment> listIssuesForMe(@RequestParam String project, @RequestParam String likes){
        PromptTemplate promptTemplate = SystemPromptTemplate.builder()
                .template("display issues with key,assigneeName, labels").build();
        Map<String, Object> params = new HashMap<>();
        params.put("project", project);
        params.put("likes", likes);
        PromptTemplate uer = PromptTemplate.builder().renderer(
                StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()).template(
                        "Find jira issues in the project <project> what would match my <likes>").variables(params).build();
        //var x = chatClient.prompt( //
        return null;
    }
}
