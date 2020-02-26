package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author growdane@gmail.com
 * @date 2020-02-15 12:27
 */

@Mapper
public interface CategoryMapper {
    //查询分类
    CategoryNode selectList();

}
