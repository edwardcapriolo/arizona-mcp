package io.teknek.arizonamcp.service;

import io.teknek.arizonamcp.livy.SessionRequest;
import io.teknek.arizonamcp.livy.SessionResponse;
import io.teknek.arizonamcp.livy.StatementRequest;
import io.teknek.arizonamcp.livy.StatementResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ArizonaService {

    private final LivyService livyService;
    public ArizonaService(LivyService livyService){
        this.livyService = livyService;
    }

    @Tool(description = "find the databases in the hive metastore")
    public List<String> findDatabases()  {
        //return List.of("default", "db1");
        SessionResponse createSession = livyService.createSession( new SessionRequest("spark", "queue"));
        SessionResponse status = null;
        int attempts = 0;
        do {
            status = livyService.findSession(createSession);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (!status.state.equalsIgnoreCase("idle") && attempts++ < 3);
        StatementResponse statement = livyService.createStatement( status, new StatementRequest("show databases"));
        StatementResponse statementStatus = null;
        attempts = 0;
        do {
            statementStatus = livyService.findStatement(createSession, statement);
            if ("available".equalsIgnoreCase(statementStatus.state)){
                if ("ok".equalsIgnoreCase(statementStatus.output.status)){
                    return List.of(statementStatus.output.data.textPlain);
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while( attempts++ < 3);

        throw new RuntimeException("did not complete");
    }
    @Tool(description = "create a temporary view with existing parquet data")
    public void createTemporaryView(String uri, String tableOrViewName){

    }

    @Tool(description = "create a training and test data from a table")
    public Map<String, String> createTrainingAndTestSet(String tableOrViewName){
        return Map.of("traning", "tablenameTraining", "test", "tablename test");
    }

}
