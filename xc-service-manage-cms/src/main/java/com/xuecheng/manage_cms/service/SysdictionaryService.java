package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author growdane@gmail.com
 * @date 2020-02-15 12:46
 */

@Service
public class SysdictionaryService {

    @Autowired
    SysDictionaryDao sysDictionaryDao;

    /**
     * 根据字典分类查询字典信息
     * @param dType
     * @return
     */
    public SysDictionary findBydType(String dType){
        return sysDictionaryDao.findBydType(dType);
    }
}
