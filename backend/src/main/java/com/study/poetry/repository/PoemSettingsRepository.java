package com.study.poetry.repository;

import com.study.poetry.entity.PoemSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoemSettingsRepository extends JpaRepository<PoemSettings, Long> {
}
