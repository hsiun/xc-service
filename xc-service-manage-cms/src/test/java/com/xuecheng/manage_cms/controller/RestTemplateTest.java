package com.xuecheng.manage_cms.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author growdane@gmail.com
 * @date 2020-02-11 15:53
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RestTemplateTest {

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRestTemplate(){

        String dataUrl = "http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f";
        ResponseEntity<Map> forEntity =
                restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        System.out.println("body = " + body);
    }
}
