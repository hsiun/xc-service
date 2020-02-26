package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 16:15
 */

@Service
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;


    /**
     * 根据用户账号查询用户信息
     * @param username
     * @return
     */
    public XcUser findXcUserByUsername(String username) {

        return xcUserRepository.findXcUserByUsername(username);
    }


    /**
     * 根据账号查询用户的信息，返回用户扩展信息
     * @param username
     * @return
     */
    public XcUserExt getUserExt(String username){
        LOGGER.info("用户名为: {}", username);
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null) {
            return null;
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        String id = xcUserExt.getId();
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(id);
        if (xcCompanyUser != null) {
            String companyId = xcCompanyUser.getCompanyId();
            xcUserExt.setCompanyId(companyId);
        }
        LOGGER.info("xc user ext : {}", xcUserExt.toString());
        return xcUserExt;
    }
}
