package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 16:13
 */

public interface XcUserRepository extends JpaRepository<XcUser, String> {
    XcUser findXcUserByUsername(String username);
}
