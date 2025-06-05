package io.teknek.arizonamcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ArizonaService {

    @Tool(description = "split a dataset to training and testing")
    public String testSplit(String uri, double testpercentage){
        return "";
    }

}
