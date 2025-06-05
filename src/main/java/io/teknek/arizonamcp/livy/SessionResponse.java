package io.teknek.arizonamcp.livy;

public class SessionResponse {
    public String state;
    public Integer id;
    public SessionResponse (){

    }

    public SessionResponse(String state, Integer id) {
        this.state = state;
        this.id = id;
    }
}
