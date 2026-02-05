package com.dogu.auth.user.api;

import java.util.List;

public interface UserService {
    public UserDto save(UserDto param);
    public UserDto get(int id);
    public List<UserDto> getAll();
    public UserDto update(UserDto info);
    public void delete(int id);
}

