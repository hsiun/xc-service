package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.CategoryNode;
import com.xuecheng.manage_course.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author growdane@gmail.com
 * @date 2020-02-15 12:33
 */

@Service
public class CategoryService {
    @Autowired
    CategoryMapper categoryMapper;

    public CategoryNode selectList(){
        return categoryMapper.selectList();
    }
}
