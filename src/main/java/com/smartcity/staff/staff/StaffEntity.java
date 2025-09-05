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
public class StaffEntity implements UserDetails {
    @Id
    @Column("id")
    private String id;
    @Column("name")
    private String name;
    @Column("email")
    private String email;
    @Column("password")
    private String password;
    @Column("role")
    private String role;
    @Column("department")
    private String department;
    @Column("is_active")
    private boolean isActive;
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(this.role.split(",")).map(SimpleGrantedAuthority::new).toList();
    }
    public List<String> getRoles() {
        return Arrays.stream(this.role.split(",")).toList();
    }

    @Override
    public String getUsername() {
        return this.id;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
