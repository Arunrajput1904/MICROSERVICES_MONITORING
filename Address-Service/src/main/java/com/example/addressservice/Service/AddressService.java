package com.example.addressservice.Service;

import com.example.addressservice.Dto.AddressDto;
import com.example.addressservice.Entity.AddressEntity;
import com.example.addressservice.ExceptionHandling.NoResourceFound;
import com.example.addressservice.Repository.AddressRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {


    private final AddressRepo addressRepo;
private final ModelMapper modelMapper;

    public AddressDto addAddress(AddressDto address) {
        AddressEntity addressEntity = modelMapper.map(address, AddressEntity.class);
        AddressEntity address1=addressRepo.save(addressEntity);
        return modelMapper.map(address1,AddressDto.class);


    }


    public AddressDto getAddress(long id){
        System.out.println("getAddress....................................................................");
        AddressEntity addressEntity=addressRepo.findByIdofAddress(id).orElseThrow(()-> new NoResourceFound("Address not found"));

        return modelMapper.map(addressEntity, AddressDto.class);
    }


    public List<AddressDto> findalladdress() {

        List<AddressEntity> addressEntity=addressRepo.findAll();

        List<AddressDto> addressDtos=addressEntity.stream().map((item)->
                modelMapper.map(item,AddressDto.class)).toList();

        return addressDtos;
    }
}
