package com.azizulhakim.monthlyplanner.repository;

import com.azizulhakim.monthlyplanner.model.MonthlyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MonthlyPlanRepository extends JpaRepository<MonthlyPlan, Long> {
    Optional<MonthlyPlan> findByYearAndMonth(int year, int month);
}
