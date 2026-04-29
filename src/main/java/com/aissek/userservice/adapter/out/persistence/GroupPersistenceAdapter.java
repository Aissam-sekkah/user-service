package com.aissek.userservice.adapter.out.persistence;

import com.aissek.userservice.adapter.out.persistence.mapper.GroupPersistenceMapper;
import com.aissek.userservice.adapter.out.persistence.repository.GroupJpaRepository;
import com.aissek.userservice.domain.model.Group;
import com.aissek.userservice.domain.port.out.GroupRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupPersistenceAdapter implements GroupRepositoryPort {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupPersistenceMapper mapper;

    @Override
    public Optional<Group> findById(String id) {
        return groupJpaRepository.findById(id).map(mapper::toDomaine);
    }

    @Override
    public List<Group> findAll() {
        return groupJpaRepository.findAll().stream()
                .map(mapper::toDomaine)
                .collect(Collectors.toList());
    }

    @Override
    public Group save(Group group) {
        var entity = mapper.toEntity(group);
        var saved = groupJpaRepository.save(entity);
        return mapper.toDomaine(saved);
    }

    @Override
    public void deleteById(String id) {
        groupJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return groupJpaRepository.existsByName(name);
    }
}
