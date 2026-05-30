package com.habitlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.habitlink.entity.TeamMember;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    @Select("SELECT * FROM team_member WHERE team_id = #{teamId} FOR UPDATE")
    List<TeamMember> selectByTeamIdForUpdate(@Param("teamId") Long teamId);
}
