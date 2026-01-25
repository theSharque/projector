package com.projector.functionalarea.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ServerWebInputException;

import com.projector.feature.model.Feature;
import com.projector.feature.repository.FeatureRepository;
import com.projector.functionalarea.model.FunctionalArea;
import com.projector.functionalarea.repository.FunctionalAreaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FunctionalAreaService {

    private final FunctionalAreaRepository functionalAreaRepository;
    private final FeatureRepository featureRepository;

    public Flux<FunctionalArea> getAllFunctionalAreas() {
        return functionalAreaRepository.findAll();
    }

    public Mono<FunctionalArea> getFunctionalAreaById(Long id) {
        return functionalAreaRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Functional area not found")));
    }

    @Transactional
    public Mono<FunctionalArea> createFunctionalArea(FunctionalArea functionalArea) {
        return validateFunctionalArea(functionalArea)
                .flatMap(valid -> {
                    LocalDateTime now = LocalDateTime.now();
                    functionalArea.setId(null);
                    functionalArea.setCreateDate(now);
                    functionalArea.setUpdateDate(now);
                    return functionalAreaRepository.save(functionalArea);
                });
    }

    @Transactional
    public Mono<FunctionalArea> updateFunctionalArea(Long id, FunctionalArea functionalArea) {
        return validateFunctionalArea(functionalArea)
                .flatMap(valid -> functionalAreaRepository.findById(id))
                .switchIfEmpty(Mono.error(new ServerWebInputException("Functional area not found")))
                .flatMap(existingFa -> {
                    functionalArea.setId(id);
                    functionalArea.setCreateDate(existingFa.getCreateDate());
                    functionalArea.setUpdateDate(LocalDateTime.now());
                    return functionalAreaRepository.save(functionalArea);
                });
    }

    @Transactional
    public Mono<Void> deleteFunctionalArea(Long id, Long replacementFaId) {
        // Validate that replacement FA ID is provided
        if (replacementFaId == null) {
            return Mono.error(new ServerWebInputException("Replacement functional area ID is required"));
        }

        // Validate that the FA to delete exists
        return functionalAreaRepository.findById(id)
                .switchIfEmpty(Mono.error(new ServerWebInputException("Functional area not found")))
                // Validate that replacement FA exists and is different
                .flatMap(faToDelete -> {
                    if (id.equals(replacementFaId)) {
                        return Mono.error(new ServerWebInputException("Replacement functional area must be different from the one being deleted"));
                    }
                    return functionalAreaRepository.findById(replacementFaId)
                            .switchIfEmpty(Mono.error(new ServerWebInputException("Replacement functional area not found")))
                            .thenReturn(faToDelete);
                })
                // Find all features using this FA
                .flatMap(faToDelete -> featureRepository.findByFunctionalAreaId(id)
                        .collectList()
                        .flatMap(features -> {
                            if (features.isEmpty()) {
                                // No features to update, just delete the FA
                                return functionalAreaRepository.deleteById(id);
                            }

                            // Update each feature: remove old FA ID and add replacement FA ID
                            List<Mono<Feature>> updateMonos = new ArrayList<>();
                            for (Feature feature : features) {
                                List<Long> faIds = new ArrayList<>(feature.getFunctionalAreaIds());
                                
                                // Remove the deleted FA ID
                                faIds.remove(id);
                                
                                // Add replacement FA ID if not already present
                                if (!faIds.contains(replacementFaId)) {
                                    faIds.add(replacementFaId);
                                }
                                
                                feature.setFunctionalAreaIds(faIds);
                                feature.setUpdateDate(LocalDateTime.now());
                                updateMonos.add(featureRepository.save(feature));
                            }

                            // Update all features, then delete the FA
                            return Flux.concat(updateMonos)
                                    .then(functionalAreaRepository.deleteById(id));
                        })
                )
                .then();
    }

    public Mono<Long> getFeaturesUsingFunctionalArea(Long id) {
        return featureRepository.findByFunctionalAreaId(id).count();
    }

    private Mono<Boolean> validateFunctionalArea(FunctionalArea functionalArea) {
        if (functionalArea.getName() == null || functionalArea.getName().trim().isEmpty()) {
            return Mono.error(new ServerWebInputException("Functional area name is required"));
        }

        return Mono.just(true);
    }
}
