package com.sho8lanatech.api.service;

import com.sho8lanatech.api.exception.ResourceNotFoundException;
import com.sho8lanatech.api.model.TrackEntity;
import com.sho8lanatech.api.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackService {

    private final TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public TrackEntity createTrack(TrackEntity track) {
        return trackRepository.save(track);
    }

    public List<TrackEntity> getAllTracks() {
        return trackRepository.findAll();
    }

    public TrackEntity getTrackById(Long id) {
        return trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found"));
    }

    public TrackEntity updateTrack(Long id, TrackEntity track) {
        TrackEntity existingTrack = getTrackById(id);

        existingTrack.setCode(track.getCode());
        existingTrack.setName(track.getName());

        return trackRepository.save(existingTrack);
    }

    public void deleteTrack(Long id) {
        trackRepository.deleteById(id);
    }

    public TrackEntity getTrackByCode(String code) {
        return trackRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Track not found with code: " + code));
    }
}
