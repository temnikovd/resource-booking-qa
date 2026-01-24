package dev.temnikov.qa_test.api.mapper;

import dev.temnikov.qa_test.api.dto.UserDto;
import dev.temnikov.qa_test.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole() != null ? user.getRole() : null,
                null
        );
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.id());
        user.setEmail(dto.email());
        user.setFullName(dto.fullName());
        user.setRole(user.getRole());
        user.setPassword(dto.password());
        return user;
    }
}
