package com.byte_sized_lessons.build_the_logic.repository;

import com.byte_sized_lessons.build_the_logic.model.Worksheet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorksheetRepository extends JpaRepository<Worksheet, Long> {
}
