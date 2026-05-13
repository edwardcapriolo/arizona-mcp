package io.teknek.arizonamcp.kubernetes;

import java.util.List;
import java.util.Map;

public record NamespaceHealthSummary(
        String namespace,
        int totalPods,
        int healthyPods,
        int unhealthyPods,
        int crashLoopingPods,
        int pendingPods,
        int restartingPods,
        Map<String, Long> statusCounts,
        List<PodHealthRow> pods
) {
}
