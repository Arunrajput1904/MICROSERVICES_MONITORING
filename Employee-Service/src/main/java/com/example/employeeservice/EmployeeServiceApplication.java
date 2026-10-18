package com.example.employeeservice;

import com.example.employeeservice.Entity.Employee;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.cloud.function.context.config.RoutingFunction;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;

import javax.xml.catalog.Catalog;
import java.util.function.Function;
import java.util.function.Supplier;

@SpringBootApplication
@EnableFeignClients
public class EmployeeServiceApplication {

    public static void main(String[] args) {
ApplicationContext context= SpringApplication.run(EmployeeServiceApplication.class, args);


    }

//    @Bean
//    public Supplier<String> supplier(){
//
//        return ()-> {
//
//            System.out.println("supplier");
//         return    "Hello World!";
//
//        };
//    }

}
