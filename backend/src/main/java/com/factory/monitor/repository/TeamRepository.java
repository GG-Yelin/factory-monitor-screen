package com.factory.monitor.repository;

import com.factory.monitor.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByCode(String code);

    List<Team> findByStatus(Integer status);

    boolean existsByCode(String code);

    boolean existsByName(String name);
}
