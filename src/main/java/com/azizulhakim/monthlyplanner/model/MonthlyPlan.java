package com.azizulhakim.monthlyplanner.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_plans", uniqueConstraints = @UniqueConstraint(columnNames = {"plan_year", "plan_month"}))
public class MonthlyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_year", nullable = false)
    private int year;

    @Column(name = "plan_month", nullable = false)
    private int month;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal income = BigDecimal.ZERO;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("position ASC, id ASC")
    private List<Expense> expenses = new ArrayList<>();

    public Long getId() { return id; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }
    public List<Expense> getExpenses() { return expenses; }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        expense.setPlan(this);
    }

    public void removeExpense(Expense expense) {
        expenses.remove(expense);
        expense.setPlan(null);
    }
}
