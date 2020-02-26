package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 14:52
 */

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //认证方法
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        //申请令牌
        AuthToken authToken = applyToken(username, password, clientId, clientSecret);
        if (authToken == null) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        String access_token = authToken.getAccess_token();
        String content = JSON.toJSONString(authToken);
        boolean saveTokenResult = saveToken(access_token, content, tokenValiditySeconds);

        if (!saveTokenResult) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    private boolean saveToken(String access_token, String content, int tokenValiditySeconds) {
        String name = "user_token:" + access_token;
        stringRedisTemplate.boundValueOps(name).set(content, tokenValiditySeconds, TimeUnit.SECONDS);

        Long expire = stringRedisTemplate.getExpire(name);
        return expire > 0;
    }

    //认证方法，可以参考测试代码中写的
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret) {
        ServiceInstance instance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        if (instance == null) {
            LOGGER.error("choose an auth instance fail");
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_AUTHSERVER_NOTFOUND);
        }
        String path = instance.getUri().toString() + "/auth/oauth/token";
        //定义body
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        //授权方式
        formData.add("grant_type", "password");
        //账号
        formData.add("username", username);
        //密码
        formData.add("password", password);
        //定义头
        MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        header.add("Authorization", httpbasic(clientId, clientSecret));

        LOGGER.info("form data : {}", formData.toString());
        LOGGER.info("header : {}", header.toString());

        HttpEntity<MultiValueMap<String, String>> multiValueMapHttpEntity = new HttpEntity<>(formData, header);
        Map map = null;
        try {
            //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                public void handleError(ClientHttpResponse response) throws IOException {
                    if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                        super.handleError(response);
                    }
                }
            });
            //远程调用申请令牌
            ResponseEntity<Map> exchange = restTemplate.exchange(path, HttpMethod.POST, multiValueMapHttpEntity, Map.class);
            map = exchange.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("request oauth_token_password error: {}", e.getMessage());
            e.printStackTrace();
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }

        if (map == null ||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null) {
            //获取错误返回
            String error_description = (String) map.get("error_description");
            if (StringUtils.isNotEmpty(error_description)) {
                if (error_description.equals("坏的凭证")) {
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                } else if (error_description.indexOf("UserDetailsService returned null") >= 0) {
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            //jti是jwt令牌的唯一标识作为用户身份令牌
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        //访问令牌(jwt)
        String jwt_token = (String) map.get("access_token");
        //刷新令牌(jwt)
        String refresh_token = (String) map.get("refresh_token");
        //jti，作为用户的身份标识
        String access_token = (String) map.get("jti");
        authToken.setJwt_token(jwt_token);
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;

    }

    private String httpbasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String string = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64.encode(string.getBytes());
        return "Basic " + new String(encode);
    }
}
