package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author growdane@gmail.com
 * @date 2020-02-11 15:46
 */

public interface CmsConfigRepository extends MongoRepository<CmsConfig,String> {
}
