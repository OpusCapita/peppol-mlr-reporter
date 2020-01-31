package com.opuscapita.peppol.mlrreporter.sender;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Configuration
public class A2AConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(A2AConfiguration.class);

    @Value("${a2a.host:''}")
    private String host;

    @Value("${a2a.username:''}")
    private String username;

    @Value("${a2a.password:''}")
    private String password;

    @Value("${a2a.timeout:3}")
    private int timeout;

    private RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectionRequestTimeout(timeout * 60 * 1000)
                .setConnectTimeout(timeout * 60 * 1000)
                .setSocketTimeout(timeout * 60 * 1000)
                .build();
    }

    private PoolingHttpClientConnectionManager getConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(100);
        manager.setDefaultMaxPerRoute(100);
        return manager;
    }

    private SSLConnectionSocketFactory getConnectionSocketFactory() throws Exception {
        try {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            return new SSLConnectionSocketFactory(sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error("Failed to disable SSL Cert Validation for A2A Endpoint", e);
            throw e;
        }
    }

    @Bean
    @Qualifier("a2aRestTemplate")
    public RestTemplate a2aRestTemplate() throws Exception {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setSSLSocketFactory(getConnectionSocketFactory())
                    .setConnectionManager(getConnectionManager())
                    .setDefaultRequestConfig(getRequestConfig())
                    .build();

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            return new RestTemplate(requestFactory);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error("Failed to configure rest template", e);
            throw e;
        }
    }

    public String getHost() {
        return host;
    }

    public String getAuthHeader() {
        byte[] basicAuthValue = (username + ":" + password).getBytes();
        return "Basic " + Base64.getEncoder().encodeToString(basicAuthValue);
    }
}
