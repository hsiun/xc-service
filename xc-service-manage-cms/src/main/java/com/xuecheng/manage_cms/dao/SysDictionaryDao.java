package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author growdane@gmail.com
 * @date 2020-02-15 12:45
 */

public interface SysDictionaryDao extends MongoRepository<SysDictionary, String> {
    //根据字典分类查询字典信息
    SysDictionary findBydType(String dType);
}
