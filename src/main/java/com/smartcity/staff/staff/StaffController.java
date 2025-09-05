package com.smartcity.staff.staff;

import com.smartcity.models.Staff;
import com.smartcity.models.StaffRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/staff")
@Slf4j
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Flux<Staff> getAll() {
        log.info("Fetching all staff");
        return staffService.getAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STAFF')")
    public Mono<Staff> get() {
        log.info("Fetching staff details");
        return staffService.get();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<ResponseEntity<String>> create(@Valid @RequestBody StaffRequest staffRequest) {
        log.info("Creating new staff");
        return staffService.create(staffRequest)
                .map(id -> ResponseEntity.status(201).body(id));
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('STAFF')")
    public Mono<ResponseEntity<Void>> update(@RequestBody Staff staff) {
        return staffService.update(staff).then(Mono.just(ResponseEntity.status(202).build()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Mono<Void> delete(@PathVariable String id) {
        return staffService.delete(id);
    }
}
