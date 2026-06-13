package com.sho8lanatech.api.repository;

import com.sho8lanatech.api.model.TrackEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    Optional<TrackEntity> findByCode(String code);
}
