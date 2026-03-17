package com.billwise.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    private final String mongoUri;

    public MongoConfig(@Value("${spring.data.mongodb.uri:}") String mongoUri) {
        // Fallback logic to ensure we never connect to localhost if Atlas info is available
        if (mongoUri == null || mongoUri.isEmpty() || mongoUri.contains("localhost")) {
            String envUri = System.getenv("MONGODB_URI");
            if (envUri == null) envUri = System.getenv("DB_URL");
            
            if (envUri != null) {
                this.mongoUri = envUri;
            } else {
                // Last resort hardcoded Atlas URL if everything else fails
                this.mongoUri = "mongodb+srv://gmkt189_db_user:Pb06MQ5rq2Y5yKGe@billwise.n18dubc.mongodb.net/billwise2-db?retryWrites=true&w=majority&appName=BillWise";
            }
        } else {
            this.mongoUri = mongoUri;
        }
    }

    @Bean
    public MongoClient mongoClient() {
        System.out.println("Connecting to MongoDB URI: " + mongoUri.replaceAll(":.*@", ":****@"));
        return MongoClients.create(mongoUri);
    }
}
