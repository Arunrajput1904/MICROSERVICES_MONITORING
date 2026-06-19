package com.example.employeeservice;

import com.example.employeeservice.Dto.EmployeeDto;
import com.example.employeeservice.Service.EmployeeService;
import com.netflix.discovery.converters.Auto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmployeeServiceApplicationTests {


    @Autowired
    EmployeeService employeeService;
    @Test
    void contextLoads() {


//        System.out.println(s+"...........................................................................................");
    }


}
