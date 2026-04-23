package com.aissek.userservice.adapter.out.persistence.mapper;

import com.aissek.userservice.adapter.out.persistence.entity.UserEntity;
import com.aissek.userservice.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {

    public UserEntity toEntity(User user){

        return new UserEntity(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getCreatedAt()
        );

    }

    public User toDomaine(UserEntity userEntity){

        return new User(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPasswordHash(),
                userEntity.getCreatedAt()
        );

    }
}
