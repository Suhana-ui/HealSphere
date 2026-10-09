package com.healsphere.service;

import com.healsphere.dto.EnvironmentDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Knows the four environments and validates environment names. */
@Service
public class EnvironmentService {

    public static final String DEFAULT_KEY = "forest";

    private final Map<String, EnvironmentDto> environments = new LinkedHashMap<>();

    public EnvironmentService() {
        add(new EnvironmentDto("forest", "Peaceful Forest",
                "Soft light through tall trees, birdsong and a gentle breeze.", "/audio/forest.mp3", "/images/forest.jpg"));
        add(new EnvironmentDto("beach", "Serene Beach",
                "Warm sand, a wide horizon and slow, rolling waves.", "/audio/beach.mp3", "/images/beach.jpg"));
        add(new EnvironmentDto("mountain", "Mountain Retreat",
                "Crisp, quiet air high above the valley with soft wind.", "/audio/mountain.mp3", "/images/mountain.jpg"));
        add(new EnvironmentDto("room", "Meditation Room",
                "A warm, minimal space with candlelight and gentle ambient tones.", "/audio/room.mp3", "/images/room.jpg"));
    }

    private void add(EnvironmentDto env) {
        environments.put(env.key(), env);
    }

    public List<EnvironmentDto> getAll() {
        return List.copyOf(environments.values());
    }

    public Optional<EnvironmentDto> find(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(environments.get(key.trim().toLowerCase(Locale.ROOT)));
    }

    /** Returns the matching environment, or the default forest when missing/invalid. */
    public EnvironmentDto resolve(String key) {
        return find(key).orElse(environments.get(DEFAULT_KEY));
    }
}
