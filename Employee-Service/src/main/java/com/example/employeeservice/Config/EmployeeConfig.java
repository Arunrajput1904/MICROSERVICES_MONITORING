package com.example.employeeservice.Config;


import com.example.employeeservice.Service.EmployeeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EmployeeConfig {


    @Bean
    public ModelMapper modelMapper(){
        return new ModelMapper();
    }


    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


    @Bean
    @LoadBalanced
    public WebClient webClient(){
        return WebClient.builder().build();
    }



}
