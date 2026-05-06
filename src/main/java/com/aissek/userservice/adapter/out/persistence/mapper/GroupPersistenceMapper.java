package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.GroupEntity;
import com.aissek.userservice.domain.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupPersistenceMapper {

    private final RolePersistenceMapper roleMapper;

    public GroupEntity toEntity(Group group) {
        GroupEntity entity = new GroupEntity();
        entity.setId(group.getId());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setCreatedAt(group.getCreatedAt());
        if (group.getRoles() != null) {
            entity.setRoles(group.getRoles().stream().map(roleMapper::toEntity).collect(Collectors.toSet()));
        }
        return entity;
    }

    public Group toDomaine(GroupEntity entity) {
        return new Group(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getRoles() != null ? entity.getRoles().stream().map(roleMapper::toDomain).collect(Collectors.toSet()) : java.util.Collections.emptySet(),
                entity.getCreatedAt()
        );
    }
}
