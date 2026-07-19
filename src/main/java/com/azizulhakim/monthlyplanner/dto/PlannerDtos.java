package com.azizulhakim.monthlyplanner.dto;

import com.azizulhakim.monthlyplanner.model.Expense;
import com.azizulhakim.monthlyplanner.model.MonthlyPlan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class PlannerDtos {
    private PlannerDtos() {}

    public record IncomeRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal income
    ) {}

    public record ExpenseRequest(
            @NotBlank @Size(max = 80) String name,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal amount,
            @NotBlank @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String color
    ) {}

    public record ExpenseResponse(Long id, String name, BigDecimal amount, String color, int position) {
        public static ExpenseResponse from(Expense expense) {
            return new ExpenseResponse(
                    expense.getId(), expense.getName(), expense.getAmount(),
                    expense.getColor(), expense.getPosition()
            );
        }
    }

    public record PlannerResponse(
            int year,
            int month,
            BigDecimal income,
            BigDecimal totalExpenses,
            BigDecimal remaining,
            BigDecimal expensePercent,
            BigDecimal availablePercent,
            List<ExpenseResponse> expenses
    ) {
        public static PlannerResponse from(MonthlyPlan plan) {
            BigDecimal total = plan.getExpenses().stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal remaining = plan.getIncome().subtract(total);
            BigDecimal spentPercent = percentage(total, plan.getIncome());
            BigDecimal availablePercent = percentage(remaining, plan.getIncome());
            List<ExpenseResponse> expenses = plan.getExpenses().stream()
                    .map(ExpenseResponse::from)
                    .toList();
            return new PlannerResponse(plan.getYear(), plan.getMonth(), plan.getIncome(), total,
                    remaining, spentPercent, availablePercent, expenses);
        }

        private static BigDecimal percentage(BigDecimal value, BigDecimal base) {
            if (base == null || base.signum() == 0) return BigDecimal.ZERO;
            return value.multiply(BigDecimal.valueOf(100)).divide(base, 1, RoundingMode.HALF_UP);
        }
    }
}
