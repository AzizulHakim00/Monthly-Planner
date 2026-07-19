package com.azizulhakim.monthlyplanner.service;

import com.azizulhakim.monthlyplanner.dto.PlannerDtos.ExpenseRequest;
import com.azizulhakim.monthlyplanner.dto.PlannerDtos.PlannerResponse;
import com.azizulhakim.monthlyplanner.model.Expense;
import com.azizulhakim.monthlyplanner.model.MonthlyPlan;
import com.azizulhakim.monthlyplanner.repository.MonthlyPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlannerService {
    private static final List<SeedExpense> DEFAULT_EXPENSES = List.of(
            new SeedExpense("Housing", new BigDecimal("24000"), "#4f46e5"),
            new SeedExpense("Food", new BigDecimal("12500"), "#f59e0b"),
            new SeedExpense("Transport", new BigDecimal("6800"), "#06b6d4"),
            new SeedExpense("Utilities", new BigDecimal("5200"), "#8b5cf6"),
            new SeedExpense("Health", new BigDecimal("3500"), "#ef4444"),
            new SeedExpense("Subscriptions", new BigDecimal("2300"), "#ec4899"),
            new SeedExpense("Personal", new BigDecimal("5000"), "#10b981")
    );

    private final MonthlyPlanRepository repository;

    public PlannerService(MonthlyPlanRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PlannerResponse getPlan(int year, int month) {
        return PlannerResponse.from(getOrCreate(year, month));
    }

    @Transactional
    public PlannerResponse updateIncome(int year, int month, BigDecimal income) {
        MonthlyPlan plan = getOrCreate(year, month);
        plan.setIncome(income);
        return PlannerResponse.from(repository.save(plan));
    }

    @Transactional
    public PlannerResponse addExpense(int year, int month, ExpenseRequest request) {
        MonthlyPlan plan = getOrCreate(year, month);
        Expense expense = new Expense();
        apply(expense, request);
        expense.setPosition(plan.getExpenses().size());
        plan.addExpense(expense);
        return PlannerResponse.from(repository.save(plan));
    }

    @Transactional
    public PlannerResponse updateExpense(int year, int month, Long expenseId, ExpenseRequest request) {
        MonthlyPlan plan = getOrCreate(year, month);
        Expense expense = findExpense(plan, expenseId);
        apply(expense, request);
        return PlannerResponse.from(repository.save(plan));
    }

    @Transactional
    public PlannerResponse deleteExpense(int year, int month, Long expenseId) {
        MonthlyPlan plan = getOrCreate(year, month);
        Expense expense = findExpense(plan, expenseId);
        plan.removeExpense(expense);
        for (int i = 0; i < plan.getExpenses().size(); i++) {
            plan.getExpenses().get(i).setPosition(i);
        }
        return PlannerResponse.from(repository.save(plan));
    }

    private MonthlyPlan getOrCreate(int year, int month) {
        validatePeriod(year, month);
        return repository.findByYearAndMonth(year, month).orElseGet(() -> createDefaultPlan(year, month));
    }

    private MonthlyPlan createDefaultPlan(int year, int month) {
        MonthlyPlan plan = new MonthlyPlan();
        plan.setYear(year);
        plan.setMonth(month);
        plan.setIncome(new BigDecimal("85000"));
        for (int i = 0; i < DEFAULT_EXPENSES.size(); i++) {
            SeedExpense seed = DEFAULT_EXPENSES.get(i);
            Expense expense = new Expense();
            expense.setName(seed.name());
            expense.setAmount(seed.amount());
            expense.setColor(seed.color());
            expense.setPosition(i);
            plan.addExpense(expense);
        }
        return repository.save(plan);
    }

    private Expense findExpense(MonthlyPlan plan, Long expenseId) {
        return plan.getExpenses().stream()
                .filter(expense -> expense.getId().equals(expenseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Expense not found in this month"));
    }

    private void apply(Expense expense, ExpenseRequest request) {
        expense.setName(request.name().trim());
        expense.setAmount(request.amount());
        expense.setColor(request.color().toLowerCase());
    }

    private void validatePeriod(int year, int month) {
        if (year < 2000 || year > 2100) throw new IllegalArgumentException("Year must be between 2000 and 2100");
        if (month < 1 || month > 12) throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    private record SeedExpense(String name, BigDecimal amount, String color) {}
}
