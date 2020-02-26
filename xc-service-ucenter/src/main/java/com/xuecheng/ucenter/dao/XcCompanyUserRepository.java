package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 16:14
 */
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser, String> {
    //根据用户id查询所属企业id
    XcCompanyUser findByUserId(String userId);
}
