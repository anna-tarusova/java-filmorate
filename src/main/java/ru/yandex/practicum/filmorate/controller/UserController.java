package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mappers.user.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.requests.UpdateUserRequest;
import ru.yandex.practicum.filmorate.requests.CreateUserRequest;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.List;
import java.util.stream.Collectors;
import static ru.yandex.practicum.filmorate.mappers.user.UserMapper.mapToUserDto;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> findAll() {
        return userService.getUsers().stream().map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserDto addUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setBirthday(request.getBirthday());
        return mapToUserDto(userService.createUser(user));
    }

    @PutMapping
    public UserDto updateUser(@RequestBody UpdateUserRequest request) {
        User user = new User();
        user.setId(request.getId());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBirthday(request.getBirthday());
        User updatedUser = userService.updateUser(user);
        return mapToUserDto(updatedUser);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }

    @GetMapping("{id}")
    public UserDto getUser(@PathVariable long id) {
        User user = userService.getUser(id);
        return mapToUserDto(user);
    }

    @PutMapping("{id}/friends/{friendId}")
    public void addToFriends(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriends(id, friendId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void removeFromFriends(@PathVariable long id, @PathVariable long friendId) {
        userService.removeFromFriends(id, friendId);
    }

    @GetMapping("{id}/friends")
    public List<UserDto> getFriends(@PathVariable long id) {
        return userService
                .getFriends(id)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.findIntersectionOfFriends(id, otherId)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }
}
