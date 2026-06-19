package com.example.employeeservice.Service;

import com.example.employeeservice.Dto.AddressDto;
import com.example.employeeservice.Dto.EmployeeAddressDto;
import com.example.employeeservice.Dto.EmployeeDto;
import com.example.employeeservice.Entity.Employee;
import com.example.employeeservice.ExceptionHandling.NoResourceFound;
import com.example.employeeservice.OtherServiceCall.AddressServiceCall;
import com.example.employeeservice.Repository.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepo employeeRepo;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;
    private final AddressServiceCall addressServiceCall;
    private final WebClient webClient;



    public void saveEmployee(EmployeeAddressDto employeeDto) {

        Employee employee=new Employee();
        employee.setName(employeeDto.getName());
        Employee employee1=employeeRepo.save(employee);

        AddressDto addressDto=new AddressDto();
        addressDto.setCity(employeeDto.getCity());
        addressDto.setEmployeeId(employee1.getId());

        ResponseEntity<AddressDto> addressDtoResponseEntity = addressServiceCall.postAddress(addressDto);

        if(!addressDtoResponseEntity.getStatusCode().is2xxSuccessful()){
            employeeRepo.deleteById(employee1.getId());

            throw new NoResourceFound("Illegal access");
        }


    }

    public EmployeeAddressDto getEmployeeById(Long id){
        Employee employee =employeeRepo.findById(id).orElseThrow(()-> new NoResourceFound("Employee not found"));

       ResponseEntity<AddressDto> addressDtoResponseEntity= addressServiceCall.getAddress(employee.getId());

       EmployeeAddressDto employeeAddressDto=new EmployeeAddressDto();
       employeeAddressDto.setName(employee.getName());
       employeeAddressDto.setCity(addressDtoResponseEntity.getBody().getCity());

       return employeeAddressDto;
    }

    public List<EmployeeAddressDto> getallEmployeeAddress() {

        List<Employee> employees=employeeRepo.findAll();
        ResponseEntity<List<AddressDto>> addressDtos=addressServiceCall.getAllAddress();


        List<EmployeeAddressDto> employeeAddressDtos=new ArrayList<>();

        addressDtos.getBody().forEach((addressDto)->{
            for(Employee employee:employees){
                if(employee.getId().equals(addressDto.getEmployeeId())){
                    employeeAddressDtos.
                            add(EmployeeAddressDto.builder()
                            .name(employee.getName())
                            .city(addressDto.getCity()).build());
                }
            }
        });


    return  employeeAddressDtos;
    }
}
