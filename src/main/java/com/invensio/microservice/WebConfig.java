package com.invensio.microservice;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import com.invensio.microservice.cors.CorsHttpInterceptor;

@Configuration
public class WebConfig {
  
//  @Bean
//  public RestTemplate restTemplate(RestTemplateBuilder builder) {
//    // Do any additional configuration here
//    return builder.build();
//  }
  
  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();

    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
    if (CollectionUtils.isEmpty(interceptors)) {
      interceptors = new ArrayList<>();
    }
    interceptors.add(new CorsHttpInterceptor());
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }
  
}
