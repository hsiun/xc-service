package com.xuecheng.manage_course.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class CourseService {

    @Autowired
    CourseMapper courseMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CoursePicRepository coursePicRepository;
    @Autowired
    CourseMarketRepository courseMarketRepository;

    public CourseMarket getCourseMarketById(String id) {
        Optional<CourseMarket> byId = courseMarketRepository.findById(id);
        return byId.orElse(null);
    }

    @Transactional
    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket one = this.getCourseMarketById(id);
        if (one != null) {
            one.setCharge(courseMarket.getCharge());
            one.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            one.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            one.setPrice(courseMarket.getPrice());
            one.setQq(courseMarket.getQq());
            one.setValid(courseMarket.getValid());
            courseMarketRepository.save(one);
        } else {
            //添加课程营销信息
            one = new CourseMarket();
            BeanUtils.copyProperties(courseMarket, one); //设置课程id
            one.setId(id);
            courseMarketRepository.save(one);
        }
        return one;
    }


    //查询课程计划
    public TeachplanNode findTeachplanList(String courseId) {
        return teachplanMapper.selectList(courseId);
    }


    public CourseBase getCourseBaseById(String id) {
        Optional<CourseBase> byId = courseBaseRepository.findById(id);
        return byId.orElse(null);
    }

    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {
        CourseBase one = getCourseBaseById(id);
        if (one == null) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //修改课程信息
        one.setName(courseBase.getName());
        one.setMt(courseBase.getMt());
        one.setSt(courseBase.getSt());
        one.setGrade(courseBase.getGrade());
        one.setStudymodel(courseBase.getStudymodel());
        one.setUsers(courseBase.getUsers());
        one.setDescription(courseBase.getDescription());
        CourseBase save = courseBaseRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase) {
        courseBase.setStatus("202001");
        CourseBase save = courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS, save.getId());
    }

    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {

        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getPname()) ||
                StringUtils.isEmpty(teachplan.getCourseid())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程id
        String courseid = teachplan.getCourseid();
        //父结点的id
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            //获取课程的根结点
            parentid = getTeachplanRoot(courseid);
        }
        //查询根结点信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan teachplan1 = optional.get();
        //父结点的级别
        String parent_grade = teachplan1.getGrade();
        //创建一个新结点准备添加
        Teachplan teachplanNew = new Teachplan();
        //将teachplan的属性拷贝到teachplanNew中
        BeanUtils.copyProperties(teachplan, teachplanNew);
        //要设置必要的属性
        teachplanNew.setParentid(parentid);
        if (parent_grade.equals("1")) {
            teachplanNew.setGrade("2");
        } else {
            teachplanNew.setGrade("3");
        }
        teachplanNew.setStatus("0");//未发布
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //获取课程的根结点
    public String getTeachplanRoot(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }
        CourseBase courseBase = optional.get();
        //调用dao查询teachplan表得到该课程的根结点（一级结点）
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplanList == null || teachplanList.size() <= 0) {
            //新添加一个课程的根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setGrade("1");//一级结点
            teachplan.setStatus("0");
            teachplan.setPname(courseBase.getName());
            teachplanRepository.save(teachplan);
            return teachplan.getId();

        }
        //返回根结点的id
        return teachplanList.get(0).getId();
    }

    /**
     * 保存课程和图片关联关系
     *
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        Optional<CoursePic> optionalCoursePic = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optionalCoursePic.isPresent()) {
            coursePic = optionalCoursePic.get();
        }
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        //保存
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursePic(String courseId) {
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        Example<CoursePic> coursePicExample = Example.of(coursePic);
        Optional<CoursePic> one = coursePicRepository.findOne(coursePicExample);
        return one.orElse(null);
    }

    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long count = coursePicRepository.deleteByCourseid(courseId);
        if (count > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    public QueryResponseResult findCourseList(int page,
                                              int size,
                                              CourseListRequest courseListRequest) {
        PageHelper.startPage(page, size);//查询第一页，每页显示10条记录
        if (courseListRequest == null) {
            courseListRequest = new CourseListRequest();
        }
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> list = courseListPage.getResult();
        long total = courseListPage.getTotal();
        QueryResult<CourseInfo> queryResult = new QueryResult<>();
        queryResult.setList(list);
        queryResult.setTotal(total);
        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }

    public CoursePic findCoursepic(String courseId) {
        CoursePic coursePic = new CoursePic();
        coursePic.setCourseid(courseId);
        Example<CoursePic> example = Example.of(coursePic);
        Optional<CoursePic> one = coursePicRepository.findOne(example);
        return one.orElse(null);
    }

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        Teachplan teachplan = optional.get();
        //只允许为叶子结点课程计划选择视频
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        TeachplanMedia one = null;
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        if (!teachplanMediaOptional.isPresent()) {
            one = new TeachplanMedia();
        } else {
            one = teachplanMediaOptional.get();
        }
        //保存媒资信息与课程计划信息
        one.setTeachplanId(teachplanId);
        one.setCourseId(teachplanMedia.getCourseId());
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        one.setMediaId(teachplanMedia.getMediaId());
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
