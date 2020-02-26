package com.xuecheng.search;


import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-19 15:24
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class IndexTest {

    @Autowired
    RestHighLevelClient client;

    //创建索引库
    @Test
    public void testCreateIndex() throws IOException {
        //创建索引请求对象，并设置索引名称
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("xc_course"); //设置索引参数
        createIndexRequest.settings(Settings.builder().put("number_of_shards",1)
                .put("number_of_replicas",0));
        createIndexRequest.mapping("doc","{\n" +
                "    \"properties\": {\n" +
                "        \"description\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"ik_max_word\",\n" +
                "            \"search_analyzer\": \"ik_smart\"\n" +
                "        },\n" +
                "        \"name\": {\n" +
                "            \"type\": \"text\",\n" +
                "            \"analyzer\": \"ik_max_word\",\n" +
                "            \"search_analyzer\": \"ik_smart\"\n" +
                "        },\n" +
                "\t\t\"pic\":{\n" +
                "\t\t\t\"type\":\"text\",\n" +
                "\t\t\t\"index\":false\n" +
                "\t\t},\n" +
                "        \"price\": {\n" +
                "            \"type\": \"float\"\n" +
                "        },\n" +
                "        \"studymodel\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"timestamp\": {\n" +
                "            \"type\": \"date\",\n" +
                "            \"format\": \"yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis\"\n" +
                "        }\n" +
                "    }\n" +
                "}", XContentType.JSON);
        //创建索引操作客户端
        IndicesClient indices = client.indices();
//创建响应对象
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest); //得到响应结果
        boolean acknowledged = createIndexResponse.isAcknowledged();
        System.out.println(acknowledged);
    }

    @Test
    public void testAddDoc() throws IOException {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "spring cloud实战");
        jsonMap.put("description", "本课程主要从四个章节进行讲解: 1.微服务架构入门 2.spring cloud\n" +
                "基础入门 3.实战Spring Boot 4.注册中心eureka。");
        jsonMap.put("studymodel", "201001");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jsonMap.put("timestap", simpleDateFormat.format(new Date()));
        jsonMap.put("price", 5.6f);

        IndexRequest indexRequest = new IndexRequest("xc_course", "doc");
        indexRequest.source(jsonMap);

        IndexResponse index = client.index(indexRequest);

        DocWriteResponse.Result result = index.getResult();
        System.out.println("result = " + result);
    }

}
