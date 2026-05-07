import io.teknek.applaws.ApplawsPolicyEngine;
import io.teknek.applaws.ApplawsPolicyModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplawPolicyEngineTest {
    @Test
    public void basicTestEngine(){

        ApplawsPolicyModel.Rule ruleEd = new ApplawsPolicyModel.Rule(ApplawsPolicyModel.Action.ALLOW,
                List.of(new ApplawsPolicyModel.Filter("[?(@.user == \"ed\")]", "When user is ed")));

        ApplawsPolicyModel.Rule ruleBob = new ApplawsPolicyModel.Rule(ApplawsPolicyModel.Action.ALLOW,
                List.of(new ApplawsPolicyModel.Filter("[?(@.user == \"bob\" && @.podname == \"webserver\")]",
                                "when user is bob and it is a webserver")));

        ApplawsPolicyEngine engine = new ApplawsPolicyEngine(List.of(ruleEd, ruleBob));
        {
            //can ed delete_pod
            Map<?,?> edwardDelete = Map.of("tool_name", "delete_pod", "user", "ed", "podname", "pod1");
            ApplawsPolicyModel.Decision decision = engine.applyPolicy(edwardDelete);
            assertEquals(ApplawsPolicyModel.Action.ALLOW, decision.action(), "thinking " + decision.trace());
        }

        {
            //can bob delete_pod
            Map<?,?> edwardDelete = Map.of("tool_name", "delete_pod", "user", "bob", "podname", "pod1");
            ApplawsPolicyModel.Decision decision = engine.applyPolicy(edwardDelete);
            assertEquals(ApplawsPolicyModel.Action.DENY, decision.action(), "thinking " + decision.trace());
        }

        {
            //can bob delete_pod webserver
            Map<?,?> edwardDelete = Map.of("tool_name", "delete_pod", "user", "bob", "podname", "webserver");
            ApplawsPolicyModel.Decision decision = engine.applyPolicy(edwardDelete);
            assertEquals(ApplawsPolicyModel.Action.ALLOW, decision.action(), "thinking " + decision.trace());
        }
    }
}
