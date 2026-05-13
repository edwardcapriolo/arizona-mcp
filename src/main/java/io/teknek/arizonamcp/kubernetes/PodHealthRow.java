package io.teknek.arizonamcp.kubernetes;

import java.util.Map;

public record PodHealthRow(
        String name,
        String namespace,
        String phase,
        String statusReason,
        boolean ready,
        int readyContainers,
        int totalContainers,
        int restartCount,
        String podIp,
        String nodeName,
        String qosClass,
        String createdAt,
        Map<String, String> labels
) {
}
