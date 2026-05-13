package io.teknek.arizonamcp;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@ConditionalOnProperty(prefix = "jira", name = "enabled", havingValue = "true")
@Configuration
public class JiraConfig {

    @Bean
    public JiraRestClient jira(@Value("${jira.url}") String jiraUrl,
                               @Value("${jira.username}") String jiraUsername,
                               @Value("${jira.token}") String jiraToken) throws URISyntaxException {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        return factory.createWithBasicHttpAuthentication( new URI(jiraUrl), jiraUsername, jiraToken);
    }
}
