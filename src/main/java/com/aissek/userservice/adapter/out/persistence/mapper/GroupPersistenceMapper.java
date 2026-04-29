package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.GroupEntity;
import com.aissek.userservice.domain.model.Group;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GroupPersistenceMapper {

    public GroupEntity toEntity(Group group) {
        GroupEntity entity = new GroupEntity();
        entity.setId(group.getId());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setCreatedAt(group.getCreatedAt());
        return entity;
    }

    public Group toDomaine(GroupEntity entity) {
        return new Group(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
