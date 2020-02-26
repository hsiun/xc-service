package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-12 18:32
 **/
@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 页面查询方法
     *
     * @param page             页码，从1开始记数
     * @param size             每页记录数
     * @param queryPageRequest 查询条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        //条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                //条件值
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        CmsPage cmsPage = new CmsPage();
        //站点ID
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //页面别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //创建条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        //页码
        page = page - 1;
        //分页对象
        Pageable pageable = new PageRequest(page, size);
        //分页查询
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        QueryResult<CmsPage> cmsPageQueryResult = new QueryResult<CmsPage>();
        cmsPageQueryResult.setList(all.getContent());
        cmsPageQueryResult.setTotal(all.getTotalElements());
        //返回结果
        return new QueryResponseResult(CommonCode.SUCCESS, cmsPageQueryResult);
    }

    //添加页面
    public CmsPageResult add(CmsPage cmsPage) {
        CmsPage originPage = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),
                cmsPage.getSiteId(),
                cmsPage.getPageWebPath());
        //如果查不到就新增
        if (originPage != null) {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        //构造返回结果
        return new CmsPageResult(CommonCode.SUCCESS, cmsPage);

    }


    public CmsPageResult getById(String id) {
        System.out.println("id = " + id);
        CmsPage cmsPage = this.getCmsPageById(id);
        if (cmsPage != null) {
            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    private CmsPage getCmsPageById(String id) {
        Optional<CmsPage> cmsPageOptional = cmsPageRepository.findById(id);
        if (cmsPageOptional.isPresent()) {
            return cmsPageOptional.get();

        }
        return null;
    }


    public CmsPageResult update(String id, CmsPage cmsPage) {
        CmsPage originCmsPage = this.getCmsPageById(id);
        if (originCmsPage != null) {
            //更新
            //更新模板id
            originCmsPage.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            originCmsPage.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            originCmsPage.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            originCmsPage.setPageName(cmsPage.getPageName());
            //更新访问路径
            originCmsPage.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            originCmsPage.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据路径
            originCmsPage.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage newCmsPage = cmsPageRepository.save(originCmsPage);
            if (newCmsPage != null) {
                return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
            }
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    //删除页面
    public ResponseResult delete(String id) {
        CmsPageResult origin = this.getById(id);
        if (origin != null) {
            cmsPageRepository.deleteById(id);
            return ResponseResult.SUCCESS();
        }
        return ResponseResult.FAIL();
    }

    //页面静态化
    public String getPageHtml(String pageId) {
        Map model = getModelByPageId(pageId);
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        String templateContent = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(templateContent)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //执行静态化
        String html = generateHtml(templateContent, model);
        if (StringUtils.isEmpty(html)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        return html;
    }

    private Map getModelByPageId(String pageId) {

        //查询页面信息
        CmsPageResult cmsPageResult = this.getById(pageId);
        CmsPage cmsPage = cmsPageResult.getCmsPage();

        if(cmsPage == null){
        //页面不存在
        ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
                }
        //取出dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if(StringUtils.isEmpty(dataUrl)){
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL); }
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    private String getTemplateByPageId(String pageId) {
        //查询页面信息
        CmsPageResult cmsPageResult = this.getById(pageId);
        CmsPage cmsPage = cmsPageResult.getCmsPage();
        if (cmsPage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //页面模板
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String generateHtml(String templateContent, Map model) {

        Configuration configuration =
                new Configuration(Configuration.getVersion());
        StringTemplateLoader stringTemplateLoader =
                new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);

        configuration.setTemplateLoader(stringTemplateLoader);

        //获取模版
        Template template = null;
        try {
            template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public ResponseResult postPage(String pageId) {
        //执行静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    private void sendPostPage(String pageId) {
        CmsPageResult cmsPageResult = this.getById(pageId);
        CmsPage cmsPage = cmsPageResult.getCmsPage();
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        //消息内容
        String msg = JSON.toJSONString(map);
        //获取站点id作为routingKey
        String siteId = cmsPage.getSiteId();
        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId, msg);
    }

    private CmsPage saveHtml(String pageId, String pageHtml) {
        Optional<CmsPage> optionalCmsPage = cmsPageRepository.findById(pageId);
        if (!optionalCmsPage.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optionalCmsPage.get();
        //存储之前先删除
        String fileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(fileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(fileId)));
        }

        //保存html文件到GridFS
        InputStream inputStream = IOUtils.toInputStream(pageHtml);
        ObjectId store = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        String storeFileId = store.toString();
        cmsPage.setHtmlFileId(storeFileId);
        cmsPageRepository.save(cmsPage);
        return cmsPage;

    }
}
