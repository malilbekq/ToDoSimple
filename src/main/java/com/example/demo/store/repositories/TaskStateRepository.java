package com.example.demo.store.repositories;

import com.example.demo.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskStateRepository extends JpaRepository<TaskStateEntity, Long> {
}
