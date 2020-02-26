package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.response.CoursePreviewResult;
import com.xuecheng.framework.domain.cms.response.CoursePublishResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResultCode;
import com.xuecheng.manage_course.controller.CourseController;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-19 16:41
 */

@Service
public class PublishService {

    @Autowired
    CoursePubRepository coursePubRepository;

    /**
     * 保存CoursePub进数据库，可以处理更新情况
     * @param id
     * @param coursePub
     * @return
     */
    public CoursePub saveCoursePub(String id, CoursePub coursePub) {
        if (StringUtils.isNotEmpty(id)) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        Optional<CoursePub> orginCoursePub = coursePubRepository.findById(id);
        CoursePub newCoursePub = null;
        if (orginCoursePub.isPresent()) {
            newCoursePub = orginCoursePub.get();
        }

        if (newCoursePub == null) {
            newCoursePub = new CoursePub();
        }

        //把属性复制到newCoursePub，最终保存的是newCoursePub
        BeanUtils.copyProperties(coursePub, newCoursePub);
        //避免没查到的时候Id为null
        newCoursePub.setId(id);
        newCoursePub.setTimestamp(new Date());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        coursePub.setPubTime(simpleDateFormat.format(new Date()));
        CoursePub save = coursePubRepository.save(newCoursePub);
        return save;
    }

    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;
    @Autowired
    TeachplanMapper teachplanMapper;
    /**
     * 通过课程ID创建CoursePub
     * @param id
     * @return
     */
    public CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);
        //基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        courseBaseOptional.ifPresent(courseBase -> BeanUtils.copyProperties(courseBase, coursePub));
        //查询课程图片
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        coursePicOptional.ifPresent(coursePic -> BeanUtils.copyProperties(coursePic, coursePub));
        //课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        courseMarketOptional.ifPresent(courseMarket -> BeanUtils.copyProperties(courseMarket, coursePub));
        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        //将课程计划转成json
        String teachplan = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(teachplan);
        return coursePub;
    }
    @Transactional
    public CoursePublishResult publish(String courseId){
        //创建coursepub
        CoursePub coursePub = createCoursePub(courseId);
        //保存coursepub
        CoursePub coursePub1 = saveCoursePub(courseId, coursePub);
        //处理保存失败
        if (coursePub1 == null) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_VIEWERROR);
        }

        return new CoursePublishResult(CommonCode.SUCCESS, coursePub1);
    }

}
