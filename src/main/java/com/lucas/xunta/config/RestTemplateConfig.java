package com.lucas.xunta.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

/**
 * RestTemplate
 *
 * @author LucasMeng Email:a@wk2.cn
 * @since 2020/4/14 16:55
 */
@Configuration
@Slf4j
public class RestTemplateConfig {


    @Value("${proxy.hostname}")
    private String proxyHost;

    @Value("${proxy.port}")
    private Integer proxyPort;

    @Value("${proxy.username}")
    private String username;

    @Value("${proxy.password}")
    private String password;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = null;
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build();
            SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            BasicCredentialsProvider provider = new BasicCredentialsProvider();
            CloseableHttpClient httpClient = null;
            if (!proxyHost.isEmpty()) {
                log.info("????????????????????????????????????:{}", proxyHost);
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
                provider.setCredentials(AuthScope.ANY, credentials);
                httpClient = HttpClients.custom()
                        .setSSLSocketFactory(csf)
                        .setProxy(new HttpHost(proxyHost, proxyPort))
                        .setDefaultCredentialsProvider(provider)
                        .build();
            } else {
                log.info("???????????????????????????????????????");
                RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
                httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLContext(sslContext).setSSLHostnameVerifier((x, y) -> true).build();

            }
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory();

            requestFactory.setHttpClient(httpClient);
            restTemplate = new RestTemplate(requestFactory);
        } catch (Exception e) {
            log.error("??????RestTemplate?????? {}", e.getMessage());
        }
        if (null == restTemplate) {
            log.error("??????RestTemplate????????????????????????https");
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

//
//    @Bean
//    public RestTemplate restTemplate() {
//        log.info("restTemplateHttps");
//        RestTemplate restTemplate = null;
//        try {
//            SSLContext sslContext = SSLContextBuilder.create().useProtocol(SSLConnectionSocketFactory.SSL).loadTrustMaterial((x, y) -> true).build();
//            RequestConfig config = RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build();
//            CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setSSLContext(sslContext).setSSLHostnameVerifier((x, y) -> true).build();
//            HttpComponentsClientHttpRequestFactory requestFactory =
//                    new HttpComponentsClientHttpRequestFactory();
//            requestFactory.setHttpClient(httpClient);
//            restTemplate = new RestTemplate(requestFactory);
//        } catch (Exception e) {
//            log.error("??????restTemplateHttps?????? {}", e.getMessage());
//        }
//        if (null == restTemplate) {
//            log.error("??????restTemplateHttps????????????????????????https");
//            restTemplate = new RestTemplate();
//        }
//        return restTemplate;
//    }

}