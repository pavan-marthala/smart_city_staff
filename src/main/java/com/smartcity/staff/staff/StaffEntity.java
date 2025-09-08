package com.smartcity.staff.staff;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Builder
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "staff")
public class StaffEntity {
    @Id
    @Column("id")
    private String id;
    @Column("name")
    private String name;
    @Column("email")
    private String email;
    @Column("department")
    private String department;
    @Column("city_id")
    private String cityId;
    @Column("village_id")
    private String villageId;
    @CreatedDate
    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    @LastModifiedDate
    private Instant updatedAt;
    @Version
    @Column("etag")
    private Long etag;

}
