package com.projector.feature.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.feature.model.Feature;
import com.projector.feature.repository.FeatureRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureService {

    private final FeatureRepository featureRepository;

    public Flux<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    public Mono<Feature> getFeatureById(Long id) {
        return featureRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Feature not found")));
    }

    @Transactional
    public Mono<Feature> createFeature(Feature feature) {
        return validateFeature(feature)
                .flatMap(valid -> {
                    LocalDateTime now = LocalDateTime.now();
                    feature.setId(null);
                    feature.setCreateDate(now);
                    feature.setUpdateDate(now);
                    return featureRepository.save(feature);
                });
    }

    @Transactional
    public Mono<Feature> updateFeature(Long id, Feature feature) {
        return validateFeature(feature)
                .flatMap(valid -> featureRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("Feature not found")))
                .flatMap(existingFeature -> {
                    feature.setId(id);
                    feature.setCreateDate(existingFeature.getCreateDate());
                    feature.setUpdateDate(LocalDateTime.now());
                    return featureRepository.save(feature);
                });
    }

    @Transactional
    public Mono<Void> deleteFeature(Long id) {
        return featureRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Feature not found")))
                .flatMap(feature -> featureRepository.deleteById(id))
                .then();
    }

    private Mono<Boolean> validateFeature(Feature feature) {
        if (feature.getYear() == null) {
            return Mono.error(new ServerWebInputException("Feature year is required"));
        }

        if (feature.getYear() < 2000 || feature.getYear() > 2500) {
            return Mono.error(new ServerWebInputException("Feature year must be between 2000 and 2500"));
        }

        if (feature.getQuarter() == null) {
            return Mono.error(new ServerWebInputException("Feature quarter is required"));
        }

        if (feature.getAuthorId() == null) {
            return Mono.error(new ServerWebInputException("Feature author is required"));
        }

        return Mono.just(true);
    }
}

