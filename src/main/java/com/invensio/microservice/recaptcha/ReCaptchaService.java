package com.invensio.microservice.recaptcha;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

@Service
public class ReCaptchaService {
  
  @Value("${recaptcha.google.server}")
  private String recaptchaServer;

  private static final Logger log = LoggerFactory.getLogger(ReCaptchaService.class);

  private RestOperations restTemplate;

  @Autowired
  public void setRestTemplate(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }

  private HttpServletRequest request;

  @Autowired
  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public boolean validate(String reCaptchaResponse) {
    URI verifyUri = URI.create(String.format(
        "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
        recaptchaServer, reCaptchaResponse, request.getRemoteAddr()));

    try {
      ReCaptchaResponse response = restTemplate.getForObject(verifyUri, ReCaptchaResponse.class);
      log.info("response from google recaptcha: {}", response.isSuccess());
      return response.isSuccess();
    } catch (Exception ignored) {
      log.error("", ignored);
      // ignore when google services are not available
      // maybe add some sort of logging or trigger that'll alert the administrator
    }
    return false;
  }

}
