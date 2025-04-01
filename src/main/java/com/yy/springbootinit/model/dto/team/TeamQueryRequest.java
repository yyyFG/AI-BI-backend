package com.yy.springbootinit.model.dto.team;


import com.yy.springbootinit.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class TeamQueryRequest extends PageRequest implements Serializable {


    /**
     * 搜索关键词
     */
    private String searchParams;

}
