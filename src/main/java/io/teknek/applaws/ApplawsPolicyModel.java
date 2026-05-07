package io.teknek.applaws;

import java.util.List;

public class ApplawsPolicyModel {
    public enum Action {
        ALLOW,
        DENY
    }
    public record Decision(Action action, List<String> trace) {}
    public record Filter(String filter, String description){}
    public record Rule(Action action, List<Filter> filters){}
}
