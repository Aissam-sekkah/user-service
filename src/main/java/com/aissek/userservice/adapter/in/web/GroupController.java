package com.aissek.userservice.adapter.in.web;

import com.aissek.userservice.adapter.in.web.dto.GroupRequest;
import com.aissek.userservice.adapter.in.web.dto.GroupResponse;
import com.aissek.userservice.adapter.in.web.mapper.GroupWebMapper;
import com.aissek.userservice.domain.port.in.GroupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupUseCase groupUseCase;
    private final GroupWebMapper groupWebMapper;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest request) {
        var group = groupUseCase.createGroup(request.name(), request.description());
        return new ResponseEntity<>(groupWebMapper.toResponse(group), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String id) {
        return groupUseCase.getGroup(id)
                .map(group -> ResponseEntity.ok(groupWebMapper.toResponse(group)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        var groups = groupUseCase.getAllGroups().stream()
                .map(groupWebMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable String id, @RequestBody GroupRequest request) {
        var group = groupUseCase.updateGroup(id, request.name(), request.description());
        return ResponseEntity.ok(groupWebMapper.toResponse(group));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable String id) {
        groupUseCase.deleteGroup(id);
    }
}
