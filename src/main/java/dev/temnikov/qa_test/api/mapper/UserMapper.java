package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.RequestUserDto;
import dev.temnikov.qa_test.api.dto.ResponseUserDto;
import dev.temnikov.qa_test.entity.User;

public class UserMapper {

    public static ResponseUserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new ResponseUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole() != null ? user.getRole() : null
        );
    }

    public static User toEntity(RequestUserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(dto.email());
        user.setFullName(dto.fullName());
        user.setRole(dto.role());
        return user;
    }
}
