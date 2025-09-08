package com.smartcity.staff.staff;

import com.smartcity.models.City;
import com.smartcity.models.Staff;
import com.smartcity.models.Village;
import com.smartcity.models.StaffRequest;
import com.smartcity.staff.shared.exception.ResourceNotFoundException;
import com.smartcity.staff.shared.jwt.JwtToken;
import com.smartcity.staff.shared.uils.UpdateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class StaffService {
    private final StaffRepository staffRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final WebClient webClient;

    public StaffService(WebClient.Builder webClientBuilder, StaffRepository staffRepository, R2dbcEntityTemplate r2dbcEntityTemplate, PasswordEncoder passwordEncoder,Environment environment) {
        this.webClient = webClientBuilder.baseUrl(Objects.requireNonNull(environment.getProperty("smart_city.services.location-service.url"))).build();
        this.staffRepository = staffRepository;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    private Mono<Village> getVillageById(String id) {
        return getAuth().flatMap(token ->
                webClient.get().uri("/villages/{id}", id)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(Village.class));
    }

    private  Mono<String> getAuth() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> Mono.just(authentication)
                        .cast(JwtToken.class)
                        .map(JwtToken::getToken)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid token."))));
    }

    private Mono<City> getCityById(String id) {
         return getAuth().flatMap(token ->
                webClient.get().uri("/cities/{id}", id)
                        .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(City.class));
    }

    public Flux<Staff> getAll() {
        log.info("Fetching all staff");
        return staffRepository.findAll()
                .flatMap(this::getVillageAndCity);
    }

    private Mono<Staff> getVillageAndCity(StaffEntity staffEntity) {
        Mono<Village> villageMono = Mono.justOrEmpty(staffEntity.getVillageId())
                .flatMap(this::getVillageById);
        Mono<City> cityMono = Mono.justOrEmpty(staffEntity.getCityId())
                .flatMap(this::getCityById);
        return Mono.zip(villageMono.defaultIfEmpty(new Village()),
                cityMono.defaultIfEmpty(new City()),
                (village, city) -> StaffMapper.INSTANCE.toModel(staffEntity, village, city));
    }

    public Mono<Staff> get() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName())
                .flatMap(staffRepository::findById)
                .flatMap(this::getVillageAndCity)
                .switchIfEmpty(Mono.error(new RuntimeException("Staff not found")));
    }

    public Mono<String> create(StaffRequest staffRequest) {
        log.info("Creating new staff with email: {}", staffRequest.getEmail());
        return staffRepository.findByEmail(staffRequest.getEmail())
                .flatMap(existingUser -> Mono.error(new RuntimeException("Staff with email: " + staffRequest.getEmail() + " already exists")))
                .switchIfEmpty(Mono.defer(() -> validateVillageAndCity(staffRequest)))
                .cast(StaffEntity.class)
                .map(StaffEntity::getId);
    }

    private Mono<StaffEntity> validateVillageAndCity(StaffRequest staffRequest) {
        return staffRequest.getCityId() == null ? Mono.error(new ResourceNotFoundException("City is required")) : getCityById(staffRequest.getCityId().toString())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("City not found with id: " + staffRequest.getCityId())))
                .flatMap(city -> {
                    if (staffRequest.getVillageId() != null) {
                        return getVillageById(staffRequest.getVillageId().toString())
                                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Village not found with id: " + staffRequest.getVillageId())))
                                .then(createStaff(staffRequest));
                    }
                    return createStaff(staffRequest);
                });
    }

    private Mono<StaffEntity> createStaff(StaffRequest staffRequest) {
        assert staffRequest.getCityId() != null;
        assert staffRequest.getId() != null;
        assert staffRequest.getVillageId() != null;
        StaffEntity staffEntity = StaffEntity.builder().id(staffRequest.getId().toString()).name(staffRequest.getName()).email(staffRequest.getEmail()).department(staffRequest.getDepartment()).cityId(staffRequest.getCityId().toString()).villageId(staffRequest.getVillageId().toString()).build();
        return r2dbcEntityTemplate.insert(StaffEntity.class).using(staffEntity);
    }

    public Mono<Void> update(Staff staff) {
        log.info("Updating staff with id: {}", staff.getId());
        return ReactiveSecurityContextHolder.getContext().map(securityContext -> securityContext.getAuthentication().getName()).flatMap(staffRepository::findById).switchIfEmpty(Mono.error(new ResourceNotFoundException("staff not found with id: " + staff.getId()))).flatMap(staffEntity -> updateStaff(staff, staffEntity)).then();
    }

    private Mono<StaffEntity> updateStaff(Staff staff, StaffEntity staffEntity) {
        UpdateHelper.updateIfNotNull(staffEntity::setDepartment, staff.getDepartment());
        UpdateHelper.updateIfNotNull(staffEntity::setName, staff.getName());
        UpdateHelper.updateIfNotNull(staffEntity::setEmail, staff.getEmail());
        return staffRepository.save(staffEntity);
    }

    public Mono<Void> delete(String id) {
        log.info("Deleting staff with id: {}", id);
        return staffRepository.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Staff not found with id: " + id))).flatMap(userEntity -> staffRepository.deleteById(userEntity.getId()));
    }


}
