package io.teknek.arizonamcp.kubernetes;

import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PodStatusMapper {

    public PodHealthRow map(V1Pod pod) {
        int readyContainers = countReadyContainers(pod);
        int totalContainers = countTotalContainers(pod);
        int restartCount = countRestarts(pod);
        boolean ready = isPodReady(pod, readyContainers, totalContainers);

        return new PodHealthRow(
                readName(pod),
                readNamespace(pod),
                readPhase(pod),
                readStatusReason(pod, ready),
                ready,
                readyContainers,
                totalContainers,
                restartCount,
                readPodIp(pod),
                readNodeName(pod),
                readQosClass(pod),
                readCreatedAt(pod),
                readLabels(pod)
        );
    }

    public NamespaceHealthSummary summarize(String namespace, List<PodHealthRow> pods) {
        Map<String, Long> statusCounts = pods.stream()
                .collect(Collectors.groupingBy(PodHealthRow::statusReason, LinkedHashMap::new, Collectors.counting()));

        int healthyPods = (int) pods.stream().filter(this::isHealthy).count();
        int crashLoopingPods = (int) pods.stream().filter(pod -> "CrashLoopBackOff".equalsIgnoreCase(pod.statusReason())).count();
        int pendingPods = (int) pods.stream().filter(pod -> "Pending".equalsIgnoreCase(pod.statusReason())).count();
        int restartingPods = (int) pods.stream().filter(pod -> pod.restartCount() > 0).count();

        return new NamespaceHealthSummary(
                namespace,
                pods.size(),
                healthyPods,
                pods.size() - healthyPods,
                crashLoopingPods,
                pendingPods,
                restartingPods,
                statusCounts,
                pods
        );
    }

    private boolean isHealthy(PodHealthRow pod) {
        return pod.ready() && "Running".equalsIgnoreCase(pod.statusReason());
    }

    private String readStatusReason(V1Pod pod, boolean ready) {
        if (pod.getMetadata() != null && pod.getMetadata().getDeletionTimestamp() != null) {
            return "Terminating";
        }

        String initReason = findInitReason(pod);
        if (hasText(initReason)) {
            return initReason;
        }

        String waitingReason = findWaitingReason(containerStatuses(pod));
        if (hasText(waitingReason)) {
            return waitingReason;
        }

        String terminatedReason = findTerminatedReason(containerStatuses(pod));
        if (hasText(terminatedReason)) {
            return terminatedReason;
        }

        String phase = readPhase(pod);
        if ("Succeeded".equalsIgnoreCase(phase)) {
            return "Completed";
        }
        if ("Running".equalsIgnoreCase(phase) && !ready) {
            return "NotReady";
        }
        return phase;
    }

    private String findInitReason(V1Pod pod) {
        for (V1ContainerStatus status : initContainerStatuses(pod)) {
            V1ContainerState state = status.getState();
            if (state == null) {
                continue;
            }
            if (state.getTerminated() != null && state.getTerminated().getExitCode() != null && state.getTerminated().getExitCode() != 0) {
                String reason = state.getTerminated().getReason();
                return hasText(reason) ? "Init:" + reason : "Init:Error";
            }
            if (state.getWaiting() != null && hasText(state.getWaiting().getReason())) {
                return "Init:" + state.getWaiting().getReason();
            }
        }
        return null;
    }

    private String findWaitingReason(List<V1ContainerStatus> statuses) {
        for (V1ContainerStatus status : statuses) {
            V1ContainerState state = status.getState();
            if (state != null && state.getWaiting() != null && hasText(state.getWaiting().getReason())) {
                return state.getWaiting().getReason();
            }
        }
        return null;
    }

    private String findTerminatedReason(List<V1ContainerStatus> statuses) {
        for (V1ContainerStatus status : statuses) {
            V1ContainerState state = status.getState();
            if (state != null && state.getTerminated() != null) {
                String reason = state.getTerminated().getReason();
                if (hasText(reason)) {
                    return reason;
                }
            }
        }
        return null;
    }

    private boolean isPodReady(V1Pod pod, int readyContainers, int totalContainers) {
        for (V1PodCondition condition : podConditions(pod)) {
            if ("Ready".equalsIgnoreCase(condition.getType())) {
                return "True".equalsIgnoreCase(condition.getStatus());
            }
        }
        return totalContainers > 0 && readyContainers == totalContainers;
    }

    private int countReadyContainers(V1Pod pod) {
        return (int) containerStatuses(pod).stream().filter(status -> Boolean.TRUE.equals(status.getReady())).count();
    }

    private int countTotalContainers(V1Pod pod) {
        if (pod.getSpec() != null && pod.getSpec().getContainers() != null) {
            return pod.getSpec().getContainers().size();
        }
        return containerStatuses(pod).size();
    }

    private int countRestarts(V1Pod pod) {
        return containerStatuses(pod).stream()
                .map(V1ContainerStatus::getRestartCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String readName(V1Pod pod) {
        return pod.getMetadata() == null ? null : pod.getMetadata().getName();
    }

    private String readNamespace(V1Pod pod) {
        return pod.getMetadata() == null ? null : pod.getMetadata().getNamespace();
    }

    private String readPhase(V1Pod pod) {
        if (pod.getStatus() == null || !hasText(pod.getStatus().getPhase())) {
            return "Unknown";
        }
        return pod.getStatus().getPhase();
    }

    private String readPodIp(V1Pod pod) {
        return pod.getStatus() == null ? null : pod.getStatus().getPodIP();
    }

    private String readNodeName(V1Pod pod) {
        return pod.getSpec() == null ? null : pod.getSpec().getNodeName();
    }

    private String readQosClass(V1Pod pod) {
        return pod.getStatus() == null ? null : pod.getStatus().getQosClass();
    }

    private String readCreatedAt(V1Pod pod) {
        if (pod.getMetadata() == null || pod.getMetadata().getCreationTimestamp() == null) {
            return null;
        }
        return pod.getMetadata().getCreationTimestamp().toString();
    }

    private Map<String, String> readLabels(V1Pod pod) {
        if (pod.getMetadata() == null || pod.getMetadata().getLabels() == null) {
            return Collections.emptyMap();
        }
        return Map.copyOf(pod.getMetadata().getLabels());
    }

    private List<V1ContainerStatus> containerStatuses(V1Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getContainerStatuses() == null) {
            return List.of();
        }
        return pod.getStatus().getContainerStatuses();
    }

    private List<V1ContainerStatus> initContainerStatuses(V1Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getInitContainerStatuses() == null) {
            return List.of();
        }
        return pod.getStatus().getInitContainerStatuses();
    }

    private List<V1PodCondition> podConditions(V1Pod pod) {
        if (pod.getStatus() == null || pod.getStatus().getConditions() == null) {
            return List.of();
        }
        return pod.getStatus().getConditions();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
