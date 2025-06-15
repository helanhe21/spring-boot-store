package com.codewithmosh.store.dtos;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String email;
}
