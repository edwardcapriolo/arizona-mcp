package io.teknek.applaws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplawsPolicyEngine {
    private final List<ApplawsPolicyModel.Rule> rules;
    ParseContext parseContext = JsonPath.using(Configuration.builder()
            .jsonProvider(new JacksonJsonProvider( new ObjectMapper()))
            .mappingProvider(new JacksonMappingProvider( new ObjectMapper())).build());
    public ApplawsPolicyEngine(List<ApplawsPolicyModel.Rule> rules){
        this.rules = rules;
    }

    public ApplawsPolicyModel.Decision applyPolicy(Map<?, ?> request){
        DocumentContext dc = parseContext.parse(request);
        List<String> trace = new ArrayList<>();
        TypeRef<List<Map<String, Object>>> typeRef = new TypeRef<List<Map<String, Object>>>() {};
        for (ApplawsPolicyModel.Rule rule : rules){
            int matchCount = 0;
            for (ApplawsPolicyModel.Filter f : rule.filters()){
                Object o = dc.read(f.filter(), typeRef);
                if (o == null){
                    trace.add(f.description() + " null return. Rule does not match");
                    break;
                } else if  (!(o instanceof List)){
                    trace.add(f.description() + " returned non bool value " + o.getClass() + " Rule specification is bad");
                    break;
                } else if (((List<?>) o).isEmpty() ){
                    trace.add(f.description() + " returned false. Rule does not match");
                    break;
                } else {
                    matchCount++;
                    trace.add(f.description() + " returned true. continue eval ");
                }
            }
            if (matchCount == rule.filters().size()){
                return new ApplawsPolicyModel.Decision(rule.action(), trace);
            }
        }
        return new ApplawsPolicyModel.Decision(ApplawsPolicyModel.Action.DENY, trace);
    }
}
