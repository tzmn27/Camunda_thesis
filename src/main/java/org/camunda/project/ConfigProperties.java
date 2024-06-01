package org.camunda.project;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("config")
public record ConfigProperties(String apiToken) {

}
