package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-22 16:34
 */

public interface TeachplanMediaRepository extends MongoRepository<TeachplanMedia, String> {
}
