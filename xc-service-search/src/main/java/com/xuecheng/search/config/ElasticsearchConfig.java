package com.xuecheng.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-19 15:07
 */


@Configuration
public class ElasticsearchConfig {

    @Value("${xuecheng.elasticsearch.hostlist}")
    private String hostList;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] hostArray = hostList.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];

        for (int i = 0; i < hostArray.length; i++) {
            String host = hostArray[i];
            httpHosts[i] = new HttpHost(host.split(":")[0],
                    Integer.parseInt(host.split(":")[1]),
                    "http");
        }
        return new RestHighLevelClient(RestClient.builder(httpHosts));
    }
}
