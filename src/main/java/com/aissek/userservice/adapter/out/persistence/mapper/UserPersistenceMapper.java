package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.GroupEntity;
import com.aissek.userservice.adapter.out.persistence.entity.UserEntity;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

    private final RolePersistenceMapper roleMapper;

    public UserEntity toEntity(User user){

        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setRefreshToken(user.getRefreshToken());
        entity.setFailedLoginAttempts(user.getFailedLoginAttempts());
        entity.setLockedUntil(user.getLockedUntil());
        entity.setVersion(user.getVersion());
        entity.setGroups(user.getGroups().stream().map(this::mapGroupToEntity).collect(Collectors.toSet()));
        entity.setDirectRoles(user.getDirectRoles().stream().map(roleMapper::toEntity).collect(Collectors.toSet()));
        entity.setCreatedAt(user.getCreatedAt());
        return entity;
    }

    public User toDomaine(UserEntity userEntity){

        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPasswordHash(),
                userEntity.getRefreshToken(),
                userEntity.getGroups().stream().map(this::mapGroupToDomaine).collect(Collectors.toSet()),
                userEntity.getDirectRoles().stream().map(roleMapper::toDomain).collect(Collectors.toSet()),
                userEntity.getFailedLoginAttempts(),
                userEntity.getLockedUntil(),
                userEntity.getVersion(),
                userEntity.getCreatedAt()
        );

    }

    private GroupEntity mapGroupToEntity(Group group) {
        GroupEntity entity = new GroupEntity();
        entity.setId(group.getId());
        entity.setName(group.getName());
        entity.setDescription(group.getDescription());
        entity.setCreatedAt(group.getCreatedAt());
        return entity;
    }

    private Group mapGroupToDomaine(GroupEntity entity) {
        return new Group(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getRoles() != null ? entity.getRoles().stream().map(roleMapper::toDomain).collect(Collectors.toSet()) : java.util.Collections.emptySet(),
                entity.getCreatedAt()
        );
    }
}
