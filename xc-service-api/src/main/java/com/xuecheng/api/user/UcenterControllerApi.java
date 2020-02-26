package com.xuecheng.api.user;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 16:12
 */

@Api(value = "用户中心",description = "用户中心管理")
public interface UcenterControllerApi {
    XcUserExt getUserext(String username);
}
