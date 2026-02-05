package com.dogu.auth.user.impl;

import com.dogu.auth.events.UserEventPublisher;
import com.dogu.auth.events.UserRegisteredEvent;
import com.dogu.auth.user.api.UserDto;
import com.dogu.auth.user.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserEventPublisher eventPublisher;

    @Override
    public UserDto save(UserDto param) {
        User user = toEntity(param, null);
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        userRepository.save(user);

        // Publish user registered event
        eventPublisher.publishUserRegistered(
                new UserRegisteredEvent(user.getId(), user.getEmail(), user.getName())
        );

        return toDto(user);
    }

    @Override
    public UserDto get(int id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(entity);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDto).toList();
    }

    @Override
    public UserDto update(UserDto info) {
        User user = userRepository.findById(info.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user = toEntity(info, user);
        return toDto(userRepository.save(user));
    }

    @Override
    public void delete(int id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(entity);
    }

    public UserDto login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return toDto(user);
    }

    public void forgotPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private User toEntity(UserDto request, User entity) {
        if (entity == null) {
            entity = new User();
        }
        entity.setName(request.getName());
        entity.setSurname(request.getSurname());
        entity.setEmail(request.getEmail());
        entity.setPassword(request.getPassword());
        entity.setPhone(request.getPhone());
        return entity;
    }

    private UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSurname(entity.getSurname());
        dto.setEmail(entity.getEmail());
        dto.setPassword(entity.getPassword());
        dto.setPhone(entity.getPhone());
        return dto;
    }
}