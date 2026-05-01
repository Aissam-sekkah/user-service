package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.RoleRequest;
import com.aissek.userservice.adapter.in.web.dto.RoleResponse;
import com.aissek.userservice.adapter.in.web.mapper.RoleWebMapper;
import com.aissek.userservice.domain.model.Role;
import com.aissek.userservice.domain.port.in.RoleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleUseCase roleUseCase;
    private final RoleWebMapper mapper;

    @PostMapping
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody RoleRequest request) {
        log.info("REST request to create role: {}", request.name());
        Role role = roleUseCase.createRole(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(role));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAll() {
        return ResponseEntity.ok(roleUseCase.getAllRoles().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList()));
    }

    @PutMapping("/user/{userId}/{roleId}")
    public ResponseEntity<Void> assignToUser(@PathVariable String userId, @PathVariable String roleId) {
        log.info("Assigning role {} to user {}", roleId, userId);
        roleUseCase.assignRoleToUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/group/{groupId}/{roleId}")
    public ResponseEntity<Void> assignToGroup(@PathVariable String groupId, @PathVariable String roleId) {
        log.info("Assigning role {} to group {}", roleId, groupId);
        roleUseCase.assignRoleToGroup(groupId, roleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/{roleId}")
    public ResponseEntity<Void> removeFromUser(@PathVariable String userId, @PathVariable String roleId) {
        roleUseCase.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/group/{groupId}/{roleId}")
    public ResponseEntity<Void> removeFromGroup(@PathVariable String groupId, @PathVariable String roleId) {
        roleUseCase.removeRoleFromGroup(groupId, roleId);
        return ResponseEntity.noContent().build();
    }
}
