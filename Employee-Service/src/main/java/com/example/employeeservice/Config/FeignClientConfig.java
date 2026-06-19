package com.example.employeeservice.Config;


import com.netflix.spectator.impl.Config;
import feign.Feign;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;

@LoadBalancerClient(name = "address-service", configuration =  randomcallConfig.class)
public class FeignClientConfig {
    @LoadBalanced
    @Bean
    public Feign.Builder fiegnbuilder(){
        return Feign.builder();
    }
}
