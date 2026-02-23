package com.dogu.auth.user.impl;

import com.dogu.auth.events.UserEventPublisher;
import com.dogu.auth.events.UserRegisteredEvent;
import com.dogu.auth.exception.EmailAlreadyExistsException;
import com.dogu.auth.exception.InvalidCredentialsException;
import com.dogu.auth.exception.UserNotFoundException;
import com.dogu.auth.user.api.UserDto;
import com.dogu.auth.user.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @CacheEvict(value = "users-all", allEntries = true)
    public UserDto save(UserDto param) {
        // Email kontrolü
        User existingUser = userRepository.findByEmail(param.getEmail());
        if (existingUser != null) {
            throw new EmailAlreadyExistsException(param.getEmail());
        }

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
    @Cacheable(value = "users", key = "#id")
    public UserDto get(int id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toDto(entity);
    }

    @Override
    @Cacheable(value = "users-all")
    public List<UserDto> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::toDto).toList();
    }

    @Override
    @Caching(
            put = {@CachePut(value = "users", key = "#info.id")},
            evict = {@CacheEvict(value = "users-all", allEntries = true)}
    )
    public UserDto update(UserDto info) {
        User user = userRepository.findById(info.getId())
                .orElseThrow(() -> new UserNotFoundException(info.getId()));
        user = toEntity(info, user);
        return toDto(userRepository.save(user));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "users-all", allEntries = true)
    })
    public void delete(int id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(entity);
    }

    public UserDto login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return toDto(user);
    }

    @CacheEvict(value = {"users", "users-all"}, allEntries = true)
    public void forgotPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "users-all", allEntries = true)
    })
    public void changePassword(int userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public int migratePasswords() {
        List<User> users = userRepository.findAll();
        int migratedCount = 0;
        for (User user : users) {
            // BCrypt hash "$2a$" ile başlar, zaten hashlenmiş ise atla
            if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userRepository.save(user);
                migratedCount++;
            }
        }
        return migratedCount;
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