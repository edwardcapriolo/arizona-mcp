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

    /**
     * Retrieve logs from one or more pods.
     *
     * @param namespace       Kubernetes namespace. If null/blank, defaults to configured default namespace.
     * @param podName        Name of a single pod. Mutually exclusive with labelSelector.
     * @param labelSelector  Label selector (e.g. "app=my-service"). Mutually exclusive with podName.
     * @param container      Optional container name. If null, Kubernetes default behavior is used.
     * @param tailLines      Optional number of most recent log lines to return (per pod). Defaults to 200.
     * @param sinceSeconds   Optional relative time window in seconds. Only logs newer than "now - sinceSeconds" are returned.
     *                       For example, 300 means "logs from the last 5 minutes".
     *                       This is evaluated by the Kubernetes API server, not this service.
     */
    @Tool(description = "Get logs from Kubernetes pods (by name or label selector)", name = "k8s_get_pod_logs")
    public String getPodLogs(String namespace,
                             String podName,
                             String labelSelector,
                             String container,
                             Integer tailLines,
                             Integer sinceSeconds) {

        if (( !hasText(podName) && !hasText(labelSelector)) || (hasText(podName) && hasText(labelSelector))) {
            throw new IllegalArgumentException("Provide either podName or labelSelector (but not both)");
        }

        String resolvedNamespace = resolveNamespace(namespace);
        int resolvedTail = tailLines != null ? tailLines : 200;

        try {
            List<String> podNames;

            if (hasText(podName)) {
                podNames = List.of(podName);
            } else {
                podNames = listPods(resolvedNamespace, labelSelector).getItems().stream()
                        .map(p -> p.getMetadata().getName())
                        .sorted()
                        .toList();
            }

            if (podNames.isEmpty()) {
                return "No pods found";
            }

            StringBuilder result = new StringBuilder();

            for (String pod : podNames) {
                String logs = coreV1Api.readNamespacedPodLog(pod, resolvedNamespace)
                        .container(container)
                        .tailLines(resolvedTail)
                        .sinceSeconds(sinceSeconds)
                        .follow(false)
                        .execute();

                if (logs == null || logs.isBlank()) {
                    continue;
                }

                for (String line : logs.split("\n")) {
                    result.append("[")
                            .append(pod)
                            .append("] ")
                            .append(line)
                            .append("\n");
                }
            }

            return result.length() > 0 ? result.toString() : "No logs found";

        } catch (ApiException e) {
            throw new IllegalStateException(
                    "Failed to read logs in namespace '" + resolvedNamespace + "': " + describeApiException(e),
                    e
            );
        }
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
