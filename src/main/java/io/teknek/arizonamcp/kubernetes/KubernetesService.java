package io.teknek.arizonamcp.kubernetes;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1PodList;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "kubernetes", name = "enabled", havingValue = "true")
public class KubernetesService {

    private final CoreV1Api coreV1Api;
    private final PodStatusMapper podStatusMapper;
    private final String defaultNamespace;

    public KubernetesService(CoreV1Api coreV1Api,
                             PodStatusMapper podStatusMapper,
                             @Value("${kubernetes.default-namespace:default}") String defaultNamespace) {
        this.coreV1Api = coreV1Api;
        this.podStatusMapper = podStatusMapper;
        this.defaultNamespace = defaultNamespace;
    }

    @Tool(description = "List pod health in a Kubernetes namespace", name = "k8s_list_pod_health")
    public List<PodHealthRow> listPodHealth(String namespace, String labelSelector) {
        String resolvedNamespace = resolveNamespace(namespace);
        return listPods(resolvedNamespace, labelSelector).getItems().stream()
                .map(podStatusMapper::map)
                .toList();
    }

    @Tool(description = "Summarize unhealthy Kubernetes pods in a namespace", name = "k8s_summarize_namespace_health")
    public NamespaceHealthSummary summarizeNamespaceHealth(String namespace, String labelSelector) {
        String resolvedNamespace = resolveNamespace(namespace);
        List<PodHealthRow> pods = listPodHealth(resolvedNamespace, labelSelector);
        return podStatusMapper.summarize(resolvedNamespace, pods);
    }

    @Bean
    public ToolCallbackProvider kubernetesTools() {
        return MethodToolCallbackProvider.builder().toolObjects(this).build();
    }

    private V1PodList listPods(String namespace, String labelSelector) {
        try {
            CoreV1Api.APIlistNamespacedPodRequest request = coreV1Api.listNamespacedPod(namespace);
            if (hasText(labelSelector)) {
                request.labelSelector(labelSelector);
            }
            V1PodList podList = request.execute();
            if (podList.getItems() == null) {
                podList.setItems(Collections.emptyList());
            }
            return podList;
        } catch (ApiException e) {
            throw new IllegalStateException("Failed to list pods in namespace '" + namespace + "': " + describeApiException(e), e);
        }
    }

    private String describeApiException(ApiException e) {
        StringBuilder message = new StringBuilder();
        message.append("status=").append(e.getCode());
        if (hasText(e.getResponseBody())) {
            message.append(", response=").append(e.getResponseBody());
        } else if (hasText(e.getMessage())) {
            message.append(", message=").append(e.getMessage());
        }
        return message.toString();
    }

    private String resolveNamespace(String namespace) {
        return hasText(namespace) ? namespace : defaultNamespace;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
