package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-22 16:10
 */

@Api(value = "媒体文件管理", description = "媒体文件管理接口", tags = {"媒体文件管理接口"})
public interface MediaFileControllerApi {
    @ApiOperation("查询文件列表")
    QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);
}