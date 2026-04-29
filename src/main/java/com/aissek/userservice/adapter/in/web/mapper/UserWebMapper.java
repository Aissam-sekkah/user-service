package com.aissek.userservice.adapter.in.web.mapper;

import com.aissek.userservice.adapter.in.web.dto.UserResponse;
import com.aissek.userservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserWebMapper {

    private final GroupWebMapper groupWebMapper;

    public UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getGroups().stream().map(groupWebMapper::toResponse).collect(Collectors.toList())
        );
    }

}
