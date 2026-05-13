package io.teknek.arizonamcp.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "jira", name = "enabled", havingValue = "true")
public class JiraService {

    private final JiraRestClient jiraRestClient;
    public JiraService(JiraRestClient jiraRestClient){
        this.jiraRestClient = jiraRestClient;
    }

    public record SerializedComment(String body, String author) {}
    @Tool(description = "list comments for issue", name="list_comments_for_issue")
    public List<SerializedComment> findCommentsForIssue(String issue){
        Issue i = jiraRestClient.getIssueClient().getIssue(issue).claim();
        List<SerializedComment> comments = new ArrayList<>();
        i.getComments().forEach(comment -> {
            String author = "";
            if (comment.getAuthor() != null && comment.getAuthor().getDisplayName() != null){
                author = comment.getAuthor().getDisplayName();
            }
            comments.add(new SerializedComment(comment.getBody(), author));
        });
        return comments;
    }

    @Bean
    ToolCallbackProvider tools(){
        return MethodToolCallbackProvider.builder().toolObjects(this).build();
    }
}
