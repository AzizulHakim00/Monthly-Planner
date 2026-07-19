package com.azizulhakim.monthlyplanner.controller;

import com.azizulhakim.monthlyplanner.dto.PlannerDtos.ExpenseRequest;
import com.azizulhakim.monthlyplanner.dto.PlannerDtos.IncomeRequest;
import com.azizulhakim.monthlyplanner.dto.PlannerDtos.PlannerResponse;
import com.azizulhakim.monthlyplanner.service.PlannerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planner/{year}/{month}")
public class PlannerController {
    private final PlannerService plannerService;

    public PlannerController(PlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @GetMapping
    public PlannerResponse getPlan(@PathVariable int year, @PathVariable int month) {
        return plannerService.getPlan(year, month);
    }

    @PutMapping("/income")
    public PlannerResponse updateIncome(@PathVariable int year, @PathVariable int month,
                                        @Valid @RequestBody IncomeRequest request) {
        return plannerService.updateIncome(year, month, request.income());
    }

    @PostMapping("/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public PlannerResponse addExpense(@PathVariable int year, @PathVariable int month,
                                      @Valid @RequestBody ExpenseRequest request) {
        return plannerService.addExpense(year, month, request);
    }

    @PutMapping("/expenses/{expenseId}")
    public PlannerResponse updateExpense(@PathVariable int year, @PathVariable int month,
                                         @PathVariable Long expenseId,
                                         @Valid @RequestBody ExpenseRequest request) {
        return plannerService.updateExpense(year, month, expenseId, request);
    }

    @DeleteMapping("/expenses/{expenseId}")
    public PlannerResponse deleteExpense(@PathVariable int year, @PathVariable int month,
                                         @PathVariable Long expenseId) {
        return plannerService.deleteExpense(year, month, expenseId);
    }
}
