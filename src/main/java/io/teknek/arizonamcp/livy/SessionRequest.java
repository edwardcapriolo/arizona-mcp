package io.teknek.arizonamcp.livy;

import org.apache.livy.client.common.HttpMessages;

public class SessionRequest {
    public String kind;
    public String queue;

    SessionRequest(){

    }

    public SessionRequest(String kind, String queue) {
        this.kind = kind;
        this.queue = queue;
    }
}
