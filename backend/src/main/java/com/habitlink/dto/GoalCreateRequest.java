package com.habitlink.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GoalCreateRequest {

    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate endDate;
}
