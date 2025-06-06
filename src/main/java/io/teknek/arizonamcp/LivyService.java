package io.teknek.arizonamcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teknek.arizonamcp.livy.SessionRequest;
import io.teknek.arizonamcp.livy.SessionResponse;
import io.teknek.arizonamcp.livy.StatementRequest;
import io.teknek.arizonamcp.livy.StatementResponse;

import org.apache.livy.LivyClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

@Service
public class LivyService {

    private RestTemplate restTemplate;
    private HttpHeaders livyPostHeaders = new HttpHeaders();
    private HttpHeaders livyGetHeaders = new HttpHeaders();
    private String livyUrl;
    private ObjectMapper mapper;

    public LivyService(@Value("${livy.url:}") String livyUrl, RestTemplate template, ObjectMapper mapper) throws IOException {
        //org.apache.livy.LivyClient c = new LivyClientBuilder().setURI(URI.create(livyUrl)).build();
        restTemplate = template;
        this.livyUrl = livyUrl;
        this.mapper = mapper;
        livyPostHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    public SessionResponse createSession(SessionRequest request){
        ResponseEntity<SessionResponse> resp = restTemplate.exchange(livyUrl + "/sessions/",
                HttpMethod.POST, new HttpEntity<>(map(request), livyPostHeaders), SessionResponse.class);
        if (resp.getStatusCode().is2xxSuccessful()){
            return resp.getBody();
        } else {
            throw new RuntimeException("request returned " + resp.getStatusCode());
        }
    }

    public SessionResponse findSession(SessionResponse session){
        ResponseEntity<SessionResponse> resp = restTemplate.exchange(livyUrl + "/sessions/" + session.id,
                HttpMethod.GET, new HttpEntity<>(livyGetHeaders), SessionResponse.class);
        if (resp.getStatusCode().is2xxSuccessful()){
            return resp.getBody();
        } else {
            throw new RuntimeException("request returned " + resp.getStatusCode());
        }
    }


    public StatementResponse createStatement(SessionResponse session, StatementRequest request){
        ResponseEntity<StatementResponse> resp =
                restTemplate.exchange(this.livyUrl + "/sessions/" + session.id + "/statements", HttpMethod.POST,
                        new HttpEntity<>(map(request), livyPostHeaders), StatementResponse.class);
        if (resp.getStatusCode().is2xxSuccessful()){
            return resp.getBody();
        } else {
            throw new RuntimeException("request returned " + resp.getStatusCode());
        }
    }


    public StatementResponse findStatement(SessionResponse session, StatementResponse statement){
        ResponseEntity<StatementResponse> resp =
                restTemplate.exchange(this.livyUrl + "/sessions/" + session.id + "/statements/" + statement.id, HttpMethod.GET,
                        new HttpEntity<>(livyGetHeaders), StatementResponse.class);
        if (resp.getStatusCode().is2xxSuccessful()){
            return resp.getBody();
        } else {
            throw new RuntimeException("request returned " + resp.getStatusCode());
        }
    }

    private String map(Object o){
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
