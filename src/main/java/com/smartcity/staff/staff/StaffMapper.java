package com.smartcity.staff.staff;

import com.smartcity.models.City;
import com.smartcity.models.Staff;
import com.smartcity.models.StaffRequest;
import com.smartcity.models.Village;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface StaffMapper {
    public static StaffMapper INSTANCE = Mappers.getMapper(StaffMapper.class);
    StaffEntity toEntity(StaffRequest staffRequest);
    @Mapping(target = "role",expression = "java(staffEntity.getRoles())")
    @Mapping(source = "village", target = "village")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "staffEntity.id", target = "id")
    @Mapping(source = "staffEntity.name", target = "name")
    @Mapping(source = "village.id", target = "village.id")
    @Mapping(source = "village.name", target = "village.name")
    @Mapping(source = "city.id", target = "city.id")
    @Mapping(source = "city.name", target = "city.name")
    Staff toModel(StaffEntity staffEntity, Village village, City city);
}
