package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dtos.UserDto;
import com.codewithmosh.store.dtos.UserRegisterRequest;
import com.codewithmosh.store.dtos.UserUpdateRequest;
import com.codewithmosh.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserRegisterRequest request);
    void update(UserUpdateRequest request, @MappingTarget User user);
}
