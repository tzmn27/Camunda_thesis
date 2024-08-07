package org.camunda.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.Spin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/config")
@Named
public class GetPruefer implements JavaDelegate {

    private final ConfigProperties config;

    public GetPruefer(ConfigProperties config) {
        this.config = config;
    }

    @Override
    @GetMapping
    public void execute(DelegateExecution execution){
        Map<String, Object> variables = new HashMap<>();
        String token = config.apiToken();
        RestClient restClient = RestClient.builder().baseUrl("http://localhost:1337/api/").build();
        String result = restClient.get()
                .uri("pruefers/?filters[Verfuegbar][$eq]=true").header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .retrieve()
                .body(String.class);

        System.out.println(result);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(result).get("data");
            int counter = 0;

            for(JsonNode subnode : node){
                counter++;
                String prof = subnode.get("attributes").get("Nachname").asText();
                variables.put("pruefer"+counter,prof);
            }
            execution.setVariable("selectPruefer",Spin.JSON(variables));
            System.out.println(execution.getVariable("selectPruefer"));

        }catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
