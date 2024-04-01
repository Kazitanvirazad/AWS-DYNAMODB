package com.dynamodb.learn.app.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@DynamoDbBean
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private int employeeNumber;
    private String employeeName;
    private String designation;
    private int manager;
    private String hireDate;
    private int salary;
    private int commission;
    private Department department;

    @DynamoDbPartitionKey
    public int getEmployeeNumber() {
        return employeeNumber;
    }
}
