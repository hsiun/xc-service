package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.IOException;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-19 21:17
 */

@Api(value = "课程搜索",description = "课程搜索",tags = {"课程搜索"})
public interface EsCourseControllerApi {
    @ApiOperation("课程搜索")
    QueryResponseResult<CoursePub> list(int page, int size,
                                        CourseSearchParam courseSearchParam) throws IOException;
}
