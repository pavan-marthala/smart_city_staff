package com.smartcity.staff.staff;

import jakarta.validation.constraints.Size;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public interface StaffRepository extends ReactiveCrudRepository<StaffEntity, String> {
    Mono<StaffEntity> findByEmail(String email);
}
