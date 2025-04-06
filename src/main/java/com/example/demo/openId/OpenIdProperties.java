package com.example.demo.openId;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openid.google")
@Getter
@Setter
public class OpenIdProperties {

    private String state;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String grantType;

}
