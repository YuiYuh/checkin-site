package com.habitlink.dto;

import lombok.Data;

@Data
public class CheckinRequest {

    private Long goalId;

    private String content;
}
