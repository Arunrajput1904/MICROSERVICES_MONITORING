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

@SpringBootApplication
@EnableFeignClients
public class EmployeeServiceApplication {

    public static void main(String[] args) {
ApplicationContext context= SpringApplication.run(EmployeeServiceApplication.class, args);

        FunctionCatalog catalog=context.getBean(FunctionCatalog.class);

        Function<Message<String>,String> s=catalog.lookup(RoutingFunction.FUNCTION_NAME);

        Message<String>sk= MessageBuilder.withPayload("Hello World").
                setHeader("spring.cloud.function.definition","uppercase")
                .build();

        System.out.println(s.apply(sk));


    }

    @Bean
    public Function<String, String> uppercase() {
        return s -> s.toUpperCase();
    }

    @Bean
    public Function<Employee, String> add() {
        return s -> s.getName()+" kaise ji";
    }
    @Bean
    public Function<Flux<String>,Flux<String>> resolve() {
        return s-> s.map(uppercase());

    }

}
