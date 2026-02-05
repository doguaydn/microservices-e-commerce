package com.dogu.auth.user.impl;

import com.dogu.auth.user.api.UserDto;
import com.dogu.auth.user.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDto save(UserDto param) {
        User user = toEntity(param, null);
        userRepository.save(user);
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
        User user = userRepository.findByEmailAndPassword(email, password);
        if (user == null) {
            throw new RuntimeException("Invalid email or password");
        }
        return toDto(user);
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