package com.example.rest.service;

import com.example.rest.domain.User;
import com.example.rest.dto.UserCreateRequest;
import com.example.rest.dto.UserUpdateRequest;
import com.example.rest.exception.BadRequestException;
import com.example.rest.exception.NotFoundException;
import com.example.rest.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public User create(UserCreateRequest req) {
        if (repo.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already exists: " + req.getUsername());
        }
        if (repo.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already exists: " + req.getEmail());
        }
        return repo.save(new User(req.getUsername(), req.getEmail()));
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id=" + id));
    }

    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional
    public User update(Long id, UserUpdateRequest req) {
        User user = getById(id);

        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            if (repo.existsByUsername(req.getUsername())) {
                throw new BadRequestException("Username already exists: " + req.getUsername());
            }
            user.setUsername(req.getUsername());
        }

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (repo.existsByEmail(req.getEmail())) {
                throw new BadRequestException("Email already exists: " + req.getEmail());
            }
            user.setEmail(req.getEmail());
        }

        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
        }

        return repo.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getById(id);
        repo.delete(user);
    }
}
