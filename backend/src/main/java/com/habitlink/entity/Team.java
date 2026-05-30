package com.habitlink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("team")
public class Team {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long creatorId;

    private String name;

    private String description;

    private String inviteCode;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
