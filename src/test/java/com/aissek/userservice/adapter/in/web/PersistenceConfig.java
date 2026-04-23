package com.aissek.userservice.adapter.in.web;


import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaRepositories(basePackages = "com.aissek.userservice.adapter.out.persistence.repository")
@EntityScan(basePackages = "com.aissek.userservice.adapter.out.persistence.entity")
public class PersistenceConfig {
}
