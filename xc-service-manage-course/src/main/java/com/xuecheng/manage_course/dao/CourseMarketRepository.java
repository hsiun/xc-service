package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseMarket;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author growdane@gmail.com
 * @date 2020-02-15 13:44
 */

public interface CourseMarketRepository extends MongoRepository<CourseMarket, String> {
}
