package dev.temnikov.qa_test.service;

import dev.temnikov.qa_test.api.dto.UserDto;
import dev.temnikov.qa_test.api.mapper.UserMapper;
import dev.temnikov.qa_test.config.AdminConfig;
import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.entity.UserRole;
import dev.temnikov.qa_test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import dev.temnikov.qa_test.api.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdminConfig adminConfig;


    public PageResponse<UserDto> getAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(UserMapper::toDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserMapper.toDto(user);
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public UserDto create(UserDto dto, String adminSecretFromRequest) {
        if (dto.password() == null || dto.password().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required when creating a new user"
            );
        }

        if (dto.role() != null && dto.role().equals(UserRole.ADMIN)) {
            String requiredSecret = adminConfig.getAdminCreationSecret();
            if (adminSecretFromRequest == null || !adminSecretFromRequest.equals(requiredSecret)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Admin user creation is forbidden: invalid or missing X-Admin-Secret"
                );
            }
        }

        User user = UserMapper.toEntity(dto);
        user.setId(null);

        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    public UserDto update(Long id, UserDto dto, String adminSecretFromRequest) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.role() != null
                && dto.role().equals(UserRole.ADMIN)
                && existing.getRole() != UserRole.ADMIN) {

            String requiredSecret = adminConfig.getAdminCreationSecret();
            if (adminSecretFromRequest == null || !adminSecretFromRequest.equals(requiredSecret)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Changing user role to ADMIN is forbidden: invalid or missing X-Admin-Secret"
                );
            }
        }

        existing.setEmail(dto.email());
        existing.setFullName(dto.fullName());
        existing.setRole(dto.role());

        if (dto.password() != null && !dto.password().isBlank()) {
            existing.setPassword(dto.password());
        }

        User saved = userRepository.save(existing);
        return UserMapper.toDto(saved);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
