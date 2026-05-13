package io.teknek.arizonamcp.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(prefix = "kubernetes", name = "enabled", havingValue = "true")
public class KubernetesConfig {

    @Bean
    @ConditionalOnMissingBean(ApiClient.class)
    public ApiClient kubernetesApiClient(@Value("${kubernetes.kubeconfig}") String kubeconfigPath) throws IOException {
        try (Reader reader = Files.newBufferedReader(Path.of(kubeconfigPath))) {
            return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(reader)).build();
        }
    }

    @Bean
    @ConditionalOnMissingBean(CoreV1Api.class)
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }
}
