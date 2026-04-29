package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.ChangePasswordRequest;
import com.aissek.userservice.adapter.in.web.dto.CreateUserRequest;
import com.aissek.userservice.adapter.in.web.dto.UpdateUserRequest;
import com.aissek.userservice.adapter.in.web.dto.UserResponse;
import com.aissek.userservice.adapter.in.web.mapper.UserWebMapper;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import com.aissek.userservice.domain.port.in.UserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ADAPTER ENTRANT : traduit HTTP -> domain.
 * Ne contient AUCUNE logique métier, uniquement de la traduction
 */
@CrossOrigin(origins = "*") // À restreindre en production !
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;
    private final GroupUseCase groupUseCase;
    private final UserWebMapper mapper;

    /**
     *  POST /api/v1/users
     *  Create a new user
     */
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request){
        var user = userUseCase.createUser(request.name(), request.email(), request.password(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(user));
    }

    /**
     * Get /api/v1/users/{id}
     * Return existing User by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id){
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toResponse(userUseCase.getUserById(id)));
    }

    /**
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll(){
        var allUsers = userUseCase.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(allUsers.stream().map(mapper::toResponse).toList());
    }

    /**
     * PUT /api/v1/users/{id}
     * Update an existing User
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest request){
        Set<Group> groups = null;
        if (request.groupIds() != null) {
            groups = request.groupIds().stream()
                    .map(groupId -> groupUseCase.getGroup(groupId)
                            .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId)))
                    .collect(Collectors.toSet());
        }

        var user = userUseCase.updateUser(id, request.name(), request.email(), groups);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toResponse(user));
    }

    /**
     * PUT /api/v1/users/{id}/password
     * Change the password of an existing user
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable String id, @Valid @RequestBody ChangePasswordRequest request) {
        userUseCase.changePassword(id, request.currentPassword(), request.newPassword());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Delete /api/v1/users/{id}
     * Delete User by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id){
        userUseCase.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
