package com.azizulhakim.monthlyplanner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PlannerApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void plannerSupportsIncomeAndExpenseCrud() throws Exception {
        mockMvc.perform(get("/api/planner/2026/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(85000))
                .andExpect(jsonPath("$.expenses.length()").value(7));

        mockMvc.perform(put("/api/planner/2026/7/income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"income":90000}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(90000));

        mockMvc.perform(post("/api/planner/2026/7/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Education","amount":4000,"color":"#2563eb"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expenses.length()").value(8));
    }

    @Test
    void invalidMonthIsRejected() throws Exception {
        mockMvc.perform(get("/api/planner/2026/13"))
                .andExpect(status().isBadRequest());
    }
}
