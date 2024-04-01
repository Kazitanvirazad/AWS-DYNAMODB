package com.dynamodb.learn.app.config;

import com.dynamodb.learn.app.model.Employee;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDBConfig {
    private final static String REGION = System.getenv("REGION");
    private final static String TABLE_NAME = System.getenv("TABLE_NAME");
    private final static String ACCESS_KEY = System.getenv("ACCESS_KEY");
    private final static String SECRET_ACCESS_KEY = System.getenv("SECRET_ACCESS_KEY");

    @Bean(name = "employeeTable")
    public DynamoDbTable<Employee> getDynamoDBEmployeeTable() {
        AwsCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_ACCESS_KEY));
        TableSchema<Employee> tableSchema = TableSchema.fromBean(Employee.class);
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(staticCredentialsProvider)
                .build();

        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        return dynamoDbEnhancedClient.table(TABLE_NAME, tableSchema);
    }
}
