package com.yatranow.repository;

import com.yatranow.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByFromLocationAndToLocation(String fromLocation, String toLocation);

    Optional<Route> findFirstByFromLocationAndToLocation(String fromLocation, String toLocation);
}
