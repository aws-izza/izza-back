package com.izza.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SecretManagerConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String SECRET_ID = "rds!db-83437ad6-f925-4f4d-8340-f10ad49f12e9-MmfOML";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        
        // Only load secrets for dev and prod profiles
        String[] activeProfiles = environment.getActiveProfiles();
        boolean shouldLoadSecrets = false;
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "prod".equals(profile)) {
                shouldLoadSecrets = true;
                break;
            }
        }
        
        if (!shouldLoadSecrets) {
            log.info("Skipping secret loading for profiles: {}", String.join(",", activeProfiles));
            return;
        }

        try {
            log.info("Loading database credentials from AWS Secrets Manager...");
            
            SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .build();

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(SECRET_ID)
                    .build();

            GetSecretValueResponse response = client.getSecretValue(request);
            String secretString = response.secretString();
            
            log.info("Successfully retrieved secret from AWS Secrets Manager");
            
            // Parse the secret JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode secretJson = mapper.readTree(secretString);
            
            String username = secretJson.get("username").asText();
            String password = secretJson.get("password").asText();
            
            // Add the credentials to Spring's environment
            Map<String, Object> secretProperties = new HashMap<>();
            secretProperties.put("DB_USERNAME", username);
            secretProperties.put("DB_PASSWORD", password);
            
            MapPropertySource secretPropertySource = new MapPropertySource("aws-secrets", secretProperties);
            environment.getPropertySources().addFirst(secretPropertySource);
            
            log.info("Database credentials loaded successfully and added to Spring environment");
            
        } catch (Exception e) {
            log.error("Failed to load secrets from AWS Secrets Manager", e);
            throw new RuntimeException("Failed to load database credentials from AWS Secrets Manager", e);
        }
    }
}