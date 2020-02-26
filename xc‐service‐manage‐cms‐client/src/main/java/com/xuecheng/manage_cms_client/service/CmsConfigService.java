package com.xuecheng.manage_cms_client.service;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.manage_cms_client.dao.CmsConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author growdane@gmail.com
 * @date 2020-02-11 15:47
 */

@Service
public class CmsConfigService {

    @Autowired
    CmsConfigRepository cmsConfigRepository;
    //根据id查询配置管理信息
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }
}
