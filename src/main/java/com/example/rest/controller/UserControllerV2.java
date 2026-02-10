package com.example.rest.controller;


import com.example.rest.domain.User;
import com.example.rest.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {

    private final UserService service;

    public UserControllerV2(UserService service) {
        this.service = service;
    }

    // Example: V2 supports Pageable directly
    @GetMapping
    public Page<User> getUsers(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
