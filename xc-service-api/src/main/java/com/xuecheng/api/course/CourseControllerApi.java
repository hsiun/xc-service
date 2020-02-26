package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Created by Administrator.
 */

@Api(value="课程管理接口",description = "课程管理接口，提供课程的增、删、改、查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("添加课程图片")
    ResponseResult addCoursePic(String courseId,String pic);

    @ApiOperation("获取课程图片基础信息")
    CoursePic findCoursePic(String courseId);

    @ApiOperation("删除课程图片")
    ResponseResult deleteCoursePic(String courseId);

    //查询课程列表
    @ApiOperation("查询我的课程列表")
    QueryResponseResult findCourseList(
            int page,
            int size,
            CourseListRequest courseListRequest
    );

    @ApiOperation("添加课程基础信息")
    AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("获取课程基础信息")
    CourseBase getCourseBaseById(String courseId) throws RuntimeException;

    @ApiOperation("更新课程基础信息")
    ResponseResult updateCourseBase(String id,CourseBase courseBase);

    @ApiOperation("获取课程营销信息")
    CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("更新课程营销信息")
    ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);

    @ApiOperation("保存媒资信息")
    ResponseResult savemedia(TeachplanMedia teachplanMedia);

}
