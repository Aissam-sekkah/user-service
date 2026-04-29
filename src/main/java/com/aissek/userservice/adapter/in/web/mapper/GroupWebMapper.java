package com.aissek.userservice.adapter.in.web.mapper;

import com.aissek.userservice.adapter.in.web.dto.GroupRequest;
import com.aissek.userservice.adapter.in.web.dto.GroupResponse;
import com.aissek.userservice.domain.model.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupWebMapper {

    public Group toDomaine(GroupRequest request) {
        return new Group(request.name(), request.description());
    }

    public GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedAt()
        );
    }
}
