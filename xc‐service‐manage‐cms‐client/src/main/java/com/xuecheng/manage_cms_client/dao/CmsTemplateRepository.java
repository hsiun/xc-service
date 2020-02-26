package com.xuecheng.manage_cms_client.dao;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author growdane@gmail.com
 * @date 2020-02-11 15:46
 */

public interface CmsTemplateRepository extends MongoRepository<CmsTemplate,String> {
}
