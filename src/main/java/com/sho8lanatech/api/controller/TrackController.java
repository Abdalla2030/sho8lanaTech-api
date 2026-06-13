package com.sho8lanatech.api.controller;

import com.sho8lanatech.api.model.TrackEntity;
import com.sho8lanatech.api.service.TrackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracks")
public class TrackController {

    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping
    public TrackEntity createTrack(@RequestBody TrackEntity track) {
        return trackService.createTrack(track);
    }

    @GetMapping
    public List<TrackEntity> getAllTracks() {
        return trackService.getAllTracks();
    }

    @GetMapping("/{id}")
    public TrackEntity getTrackById(@PathVariable Long id) {
        return trackService.getTrackById(id);
    }

    @PutMapping("/{id}")
    public TrackEntity updateTrack(@PathVariable Long id, @RequestBody TrackEntity track) {
        return trackService.updateTrack(id, track);
    }

    @DeleteMapping("/{id}")
    public void deleteTrack(@PathVariable Long id) {
        trackService.deleteTrack(id);
    }

    @GetMapping("/code/{code}")
    public TrackEntity getTrackByCode(@PathVariable String code) {
        return trackService.getTrackByCode(code);
    }
}
