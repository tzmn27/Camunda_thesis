package org.camunda.project;

import camundajar.impl.scala.Console;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

@RequestMapping("/config")
@Named
public class CheckUser implements JavaDelegate {

    private final ConfigProperties config;

    public CheckUser(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution) throws Exception {
        String token = config.apiToken();
        String nutzer = (String) execution.getVariable("initiator");

        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();
        String result = restClient.get()
                .uri("nutzers/?filters[Name][$eq]=" + nutzer).header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(String.class);

        System.out.println(result);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(result).get("data");

            if (node.isArray()){
                for(JsonNode subnode : node){
                    String test = subnode.get("attributes").get("Studiengang").asText();
                    System.out.println(test);
                    if(test.equals("null")){
                        execution.setVariable("credentials", false);
                        System.out.println(execution.getVariable("credentials"));
                    }else{
                        execution.setVariable("credentials", true);
                        System.out.println(execution.getVariable("credentials"));
                    }
                }
            }
        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}
