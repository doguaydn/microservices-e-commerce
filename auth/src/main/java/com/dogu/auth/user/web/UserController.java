package com.dogu.auth.user.web;

import com.dogu.auth.config.JwtService;
import com.dogu.auth.user.api.UserDto;
import com.dogu.auth.user.api.UserService;
import com.dogu.auth.user.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService service;

    @Autowired
    UserServiceImpl userServiceImpl;

    @Autowired
    JwtService jwtService;

    @PostMapping
    public UserResponse save(@RequestBody UserRequest info) {
        UserDto userDto = service.save(toDto(info));
        return toResponse(userDto);
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable int id) {
        UserDto userDto = service.get(id);
        return toResponse(userDto);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        List<UserDto> users = service.getAll();
        return users.stream().map(this::toResponse).toList();
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable int id, @RequestBody UserRequest info) {
        UserDto dto = toDto(info);
        dto.setId(id);
        UserDto userDto = service.update(dto);
        return toResponse(userDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody UserLoginRequest request) {
        UserDto userDto = userServiceImpl.login(request.getEmail(), request.getPassword());
        String token = jwtService.generateToken(userDto.getId(), userDto.getEmail(), userDto.getName());
        return new LoginResponse(token, userDto.getEmail(), userDto.getName(), userDto.getSurname());
    }

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userServiceImpl.forgotPassword(request.getEmail(), request.getNewPassword());
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        userServiceImpl.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
    }

    @PostMapping("/migrate-passwords")
    public String migratePasswords() {
        int count = userServiceImpl.migratePasswords();
        return count + " users migrated to BCrypt";
    }

    public UserResponse toResponse(UserDto dto) {
        UserResponse response = new UserResponse();
        response.id = dto.getId();
        response.name = dto.getName();
        response.surname = dto.getSurname();
        response.email = dto.getEmail();
        response.phone = dto.getPhone();
        return response;
    }

    public UserDto toDto(UserRequest request) {
        UserDto dto = new UserDto();
        dto.setName(request.name);
        dto.setSurname(request.surname);
        dto.setEmail(request.email);
        dto.setPassword(request.password);
        dto.setPhone(request.phone);
        return dto;
    }
}

