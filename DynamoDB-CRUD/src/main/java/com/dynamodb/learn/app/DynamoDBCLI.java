package com.dynamodb.learn.app;

import com.dynamodb.learn.app.model.Employee;
import com.dynamodb.learn.app.service.DynamoDBService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DynamoDBCLI implements CommandLineRunner {
    @Autowired
    private DynamoDBService dynamoDBService;

    @Override
    public void run(String... args) throws Exception {
        addEmployees();
    }

    private void addEmployees() {
        List<Employee> employees = dynamoDBService.getEmployeesFromFile();
        dynamoDBService.addEmployees(employees);
    }

    private void scanEmployeesWithComm() throws JsonProcessingException {
        List<Employee> employees = dynamoDBService.scanEmployeesFromTableWithCommission();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(employees));
    }

    private void queryEmployeesWithEmpNumber() throws JsonProcessingException {
        List<Employee> employees = dynamoDBService.queryEmployeesFromTableWithEmpNumber(7499);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(employees));
    }

    private void getEmployee() throws JsonProcessingException {
        Employee employee = dynamoDBService.getEmployeeByNumber(7499);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(employee));
    }

    private void updateEmployee() {
        dynamoDBService.updateDesignation(7654, "CLERK");
    }

    private void getEmployeesPartiQL() throws JsonProcessingException {
        List<Employee> employees = dynamoDBService.getEmployeesPartiQL();
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(employees));
    }
}
