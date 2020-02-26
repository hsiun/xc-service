package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author growdane@gmail.com
 * @date 2020-02-11 21:23
 */

@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    PageService pageService;

    //接收到页面id
    @RequestMapping(value="/cms/preview/{pageId}",method = RequestMethod.GET)
    public void preview(@PathVariable("pageId")String pageId) {
        String pageHtml = pageService.getPageHtml(pageId);
        System.out.println("pageHtml = " + pageHtml);
        if (StringUtils.isNoneEmpty(pageHtml)) {
            ServletOutputStream outputStream = null;
            try {
                response.setCharacterEncoding("UTF-8");
                outputStream = response.getOutputStream();
                outputStream.write(pageHtml.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
