package com.example.addressservice.Repository;

import com.example.addressservice.Entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AddressRepo extends JpaRepository<AddressEntity,Long> {

    JdbcTemplate jdbcTemplate=new JdbcTemplate();


    @Query("select e from AddressEntity e where e.id=:id")
    Optional<AddressEntity> findByIdofAddress(@Param("id") Long id);

}
