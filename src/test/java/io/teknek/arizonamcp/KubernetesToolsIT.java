package io.teknek.arizonamcp;

import io.teknek.arizonamcp.kubernetes.PodHealthRow;
import io.teknek.arizonamcp.kubernetes.KubernetesService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = McpServerApplication.class
)
@ActiveProfiles("ittest")
class KubernetesToolsIT {

    @Autowired
    private KubernetesService kubernetesService;

    @Test
    void listsPodsFromConfiguredKubernetesCluster() {
        List<PodHealthRow> pods = kubernetesService.listPodHealth("loadtest", null);

        Assertions.assertNotNull(pods);
        System.out.println(pods);
        Assertions.assertFalse(pods.isEmpty(), "Expected pods from the loadtest namespace");

        // Fetch logs via label selector instead of single pod
        String logs = kubernetesService.getPodLogs(
                "external",
                null,
                "app.kubernetes.io/name=httpcore",
                null,
                200,
                null
        );
        System.out.println(logs);
        Assertions.assertNotNull(logs);

    }
}
