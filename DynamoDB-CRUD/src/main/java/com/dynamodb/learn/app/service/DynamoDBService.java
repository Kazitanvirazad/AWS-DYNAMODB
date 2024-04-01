package com.dynamodb.learn.app.service;

import com.dynamodb.learn.app.model.Department;
import com.dynamodb.learn.app.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DynamoDBService {
    private final static String REGION = System.getenv("REGION");
    private final static String ACCESS_KEY = System.getenv("ACCESS_KEY");
    private final static String SECRET_ACCESS_KEY = System.getenv("SECRET_ACCESS_KEY");
    private DynamoDbTable<Employee> employeeDynamoDbTable;

    public DynamoDBService(@Qualifier(value = "employeeTable") DynamoDbTable<Employee> employeeDynamoDbTable) {
        this.employeeDynamoDbTable = employeeDynamoDbTable;
    }

    public List<Employee> getEmployeesFromFile() {
        List<Employee> employees;
        TypeReference<List<Employee>> typeReference = new TypeReference<>() {
        };
        ObjectMapper mapper = new ObjectMapper();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("employees.json")))) {
            StringBuilder stringBuilder = new StringBuilder();
            reader.lines().forEach(stringBuilder::append);
            return mapper.readValue(stringBuilder.toString(), typeReference);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    public void addEmployees(List<Employee> employees) {
        employees.forEach(employeeDynamoDbTable::putItem);
    }

    public List<Employee> scanEmployeesFromTableWithCommission() {
        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .putExpressionName("#comm", "commission")
                        .putExpressionValue(":comm", AttributeValue.builder()
                                .n("0").build())
                        .expression("#comm > :comm").build())
                .build();
        return employeeDynamoDbTable.scan(scanEnhancedRequest).items().stream().toList();
    }

    public List<Employee> queryEmployeesFromTableWithEmpNumber(int empNo) {
        QueryEnhancedRequest queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(empNo).build()))
                .build();

        return employeeDynamoDbTable.query(queryEnhancedRequest).items().stream().toList();
    }

    public void deleteEmployee(int empNo) {
        DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(empNo).build()).build();

        employeeDynamoDbTable.deleteItem(deleteItemEnhancedRequest);
    }

    public Employee getEmployeeByNumber(int empNo) {
        GetItemEnhancedRequest getItemEnhancedRequest = GetItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(empNo).build()).build();
        return employeeDynamoDbTable.getItem(getItemEnhancedRequest);
    }

    public void updateDesignation(int empNo, String designation) {
        Employee employee = getEmployeeByNumber(empNo);
        if (employee != null) {
            employee.setDesignation(designation);
            UpdateItemEnhancedRequest<Employee> updateItemEnhancedRequest = UpdateItemEnhancedRequest.builder(Employee.class)
                    .item(employee)
                    .build();
            employeeDynamoDbTable.updateItem(updateItemEnhancedRequest);
        }
    }

    public List<Employee> getEmployeesPartiQL() {
        List<Employee> employees = new ArrayList<>();
        ExecuteStatementRequest executeStatementRequest = ExecuteStatementRequest.builder()
                .statement("SELECT * FROM POC_DB WHERE designation = ?")
                .parameters(AttributeValue.fromS("SALESMAN"))
                .build();

        AwsCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_ACCESS_KEY));
        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(REGION))
                .credentialsProvider(staticCredentialsProvider)
                .build();
        ExecuteStatementResponse executeStatementResponse = dynamoDbClient.executeStatement(executeStatementRequest);

        if (executeStatementResponse.hasItems()) {
            executeStatementResponse.items().forEach(item -> {
                Map<String, AttributeValue> departmentMap = item.get("department").m();
                Department department = new Department(Integer.parseInt(departmentMap.get("departmentNumber").n()),
                        departmentMap.get("departmentName").s(),
                        departmentMap.get("location").s());
                employees.add(new Employee(Integer.parseInt(item.get("employeeNumber").n()), item.get("employeeName").s(), item.get("designation").s(),
                        Integer.parseInt(item.get("manager").n()), item.get("hireDate").s(), Integer.parseInt(item.get("salary").n()),
                        Integer.parseInt(item.get("commission").n()), department));
            });
        }
        dynamoDbClient.close();
        return employees;
    }
}
