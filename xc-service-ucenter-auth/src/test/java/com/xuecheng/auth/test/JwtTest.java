package com.xuecheng.auth.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-25 11:55
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtTest {

    @Test
    public void testPasswrodEncoder() {
        String password = "111111";
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String bcrypt = "$2a$10$TJ4TmCdK.X4wv/tCqHW14.w70U3CC33CeVncD3SLmyMXMknstqKRe";
        boolean matches = passwordEncoder.matches(bcrypt, "111");
        System.out.println("matches = " + matches);
    }

    @Test
    public void testCreateJwt() {
        //证书文件
        String keyLocation = "xc.keystore";
        //密钥库密码
        String keystorePassword = "xuechengkeystore";
        //访问证书路径
        ClassPathResource resource = new ClassPathResource(keyLocation);
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, keystorePassword.toCharArray());

        String keyPassword = "xuecheng";
        String alias = "xckey";

        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, keyPassword.toCharArray());

        //私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", "123");
        tokenMap.put("name", "mrt");
        tokenMap.put("roles", "r01,r02");
        tokenMap.put("ext", "1");
        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aPrivate));
        //取出jwt令牌
        String token = jwt.getEncoded();
        System.out.println("token=" + token);
    }

    @Test
    public void testVerify() {
        String token="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHQiOiIxIiwicm9sZXMiOiJyMDEscjAyIiwibmFtZSI6Im1ydCIsImlkIjoiMTIzIn0.a3pPI4RIGiI-4gYmC5aO_5iqsVaigKb_vSmjTxx-AjtujjBTENxqt6OI4K9ppx0IO3WEebBeOKQr6po-PnQvmMl7aNHPd9KWfKD_he_0Im88nXWU2JBX0ARc85rFiIB190r7kjFqlZrj7YkhwUsccC7PqfiyKg7Y6B7Ca_l98Dx3Zv6VJwYxVU159XlB6G2NGMIRmDbJhnxLUG0zZdOKBs0BsQobU-IRRCI2bEu5zTImimfogNsGciTCS-7CMN8kO4T_rSMwEe66vPMA-IjchsubfseIptYNVu9_QHbc8RNGf3wWrBcZOmTxaCIH-zAo02u1TQXyeeafLQQVzRTt8g";
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnASXh9oSvLRLxk901HANYM6KcYMzX8vFPnH/To2R+SrUVw1O9rEX6m1+rIaMzrEKPm12qPjVq3HMXDbRdUaJEXsB7NgGrAhepYAdJnYMizdltLdGsbfyjITUCOvzZ/QgM1M4INPMD+Ce859xse06jnOkCUzinZmasxrmgNV3Db1GtpyHIiGVUY0lSO1Frr9m5dpemylaT0BV3UwTQWVW9ljm6yR3dBncOdDENumT5tGbaDVyClV0FEB1XdSKd7VjiDCDbUAUbDTG1fm3K9sx7kO1uMGElbXLgMfboJ963HEJcU01km7BmFntqI5liyKheX+HBUCD4zbYNPw236U+7QIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));
        String claims = jwt.getClaims();
        System.out.println("claims = " + claims);
        String encoded = jwt.getEncoded();
        System.out.println("encoded = " + encoded);
    }
}
