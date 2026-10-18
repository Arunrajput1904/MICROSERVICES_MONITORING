package com.example.employeeservice.Controller;

import com.example.employeeservice.Config.Success;
import com.example.employeeservice.Dto.EmailObject;
import com.example.employeeservice.Dto.EmployeeAddressDto;
import com.example.employeeservice.Dto.EmployeeDto;
import com.example.employeeservice.Service.EmployeeService;
import com.sun.net.httpserver.Authenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Consumer;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {


    private final EmployeeService employeeService;

//    @Autowired
//private  KafkaTemplate<String, EmailObject> kafkaTemplate;
    private final StreamBridge streamBridge;
    @GetMapping("/get/{id}")
    public EmployeeAddressDto getEmployee(@PathVariable long id) {

        return employeeService.getEmployeeById(id);
    }


    @PostMapping("/post")
    public ResponseEntity<Success> postEmployee(@RequestBody EmployeeAddressDto employeeDto) {
        employeeService.saveEmployee(employeeDto);
//kafkaTemplate.send("test-topics",String.valueOf(Math.random()*19),new EmailObject(employeeDto.getName(),"Email Sender"));
        streamBridge.send("employeeBridge-out-0", MessageBuilder.withPayload(employeeDto.getName())
                .setHeader(KafkaHeaders.PARTITION, 0).build());


        return new  ResponseEntity<>(new Success("Success",HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/getallEmployeewithAddress")
    public ResponseEntity<List<EmployeeAddressDto>> getAllEmployees(){
        List<EmployeeAddressDto> list=employeeService.getallEmployeeAddress();

        return new ResponseEntity<>(list,HttpStatus.OK);
    }



    Consumer<String> stringConsumer = new Consumer<String>() {
        @Override
        public void accept(String t) {
            System.out.println(t);
        }
    };
}
