package com.example.demo_sample.repository;

import com.example.demo_sample.domain.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, UUID> {
}

