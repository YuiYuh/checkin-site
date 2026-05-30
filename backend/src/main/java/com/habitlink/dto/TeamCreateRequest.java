package com.habitlink.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TeamCreateRequest {

    private String name;

    private String description;

    private String goalTitle;

    private String goalDescription;

    private LocalDate startDate;

    private LocalDate endDate;
}
