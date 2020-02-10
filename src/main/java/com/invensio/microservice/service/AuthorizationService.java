package com.invensio.microservice.service;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invensio.microservice.recaptcha.ReCaptchaService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthorizationService {

  @Autowired
  private ReCaptchaService reCaptchaService;

  @Value("${permissions.microservice.proxy.url}")
  private String proxyUrl;

  @Value("${permissions.microservice.client.login}")
  private String clientLogin;

  @Value("${permissions.microservice.client.password}")
  private String clientPassword;

  @Value("${permissions.microservice.contact.login}")
  private String contactLogin;

  @Value("${permissions.microservice.contact.password}")
  private String contactPassword;

  @Value("${permissions.microservice.contact.role}")
  private String contactRole;

  @Value("${permissions.microservice.proxy.domain}")
  private String proxyDomain;

  RestTemplate restTemplate;

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  private ObjectMapper objectMapper;

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public static volatile String systemUserBearerAccessToken = null;

  public static volatile String systemUserBearerRefreshToken = null;

  public ResponseEntity<String> systemAuthenticationRequest() {
    ResponseEntity<String> result = authenticationRequest(proxyUrl + "/oauth/token", clientLogin,
        clientPassword, contactLogin, contactPassword, proxyDomain, contactRole);
    updateSystemTokens(result);
    return result;
  }

  public ResponseEntity<?> systemAuthenticationRequest(Map<String, Object> requestBody) {
    String role = (String) requestBody.get("role");
    if (role == null) {
      role = contactRole;
    }
    String DomainKey = (String) requestBody.get("DomainKey");
    if (DomainKey == null) {
      DomainKey = proxyDomain;
    }
    if (requestBody.get("grant_type").equals("refresh_token")) {
      log.info("refresh body: {}", requestBody);
      return reloadTokenRequest(proxyUrl + "/oauth/token", clientLogin, clientPassword,
          requestBody.get("refresh_token").toString(), proxyDomain, contactRole);
    }
    String recaptchaResponse = (String) requestBody.get("g-recaptcha-response");
    if (!reCaptchaService.validate(recaptchaResponse)) {
      return getErrorRespond("reCaptcha is not right.", HttpStatus.BAD_REQUEST);
    }
    String login = (String) requestBody.get("login");
    String password = (String) requestBody.get("password");
    if (login == null) {
      return getErrorRespond("Not found login.", HttpStatus.BAD_REQUEST);
    }
    if (password == null) {
      return getErrorRespond("Not found password.", HttpStatus.BAD_REQUEST);
    }
    // String role = (String) requestBody.get("role");
    // if (role == null) {
    // role = contactRole;
    // }
    // String DomainKey = (String) requestBody.get("DomainKey");
    // if (DomainKey == null) {
    // DomainKey = proxyDomain;
    // }
    ResponseEntity<String> result = authenticationRequest(proxyUrl + "/oauth/token", clientLogin,
        clientPassword, contactLogin, contactPassword, proxyDomain, contactRole);
    updateSystemTokens(result);
    return result;
  }

  private void updateSystemTokens(ResponseEntity<String> result) throws HttpClientErrorException {
    Map<String, Object> respond;
    try {
      respond =
          objectMapper.readValue(result.getBody(), new TypeReference<Map<String, Object>>() {});
    } catch (IOException e) {
      throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Cannot parse proxy respond.");
    }
    systemUserBearerAccessToken = (String) respond.get("access_token");
    systemUserBearerRefreshToken = (String) respond.get("refresh_token");
    log.info("systemUserBearerAccessToken:{}, systemUserBearerRefreshToken:{}",
        systemUserBearerAccessToken, systemUserBearerRefreshToken);
  }

  public ResponseEntity<String> authenticationRequest(String url, String microserviceLogin,
      String microservicePassword, String contactLogin, String contactPassword, String domainKey,
      String role) throws HttpClientErrorException {
    // String microserviceLogin = "af09ea17-d47c-452d-93de-2c89157b9d5b";
    // String microservicePassword = "9da73133-87f4-419e-825d-729317275f23";
    // String url = "http://localhost:8090/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Content-Type", "application/x-www-form-urlencoded");
    headers.set("accept", "*/*");
    headers.set("cache-control", "no-cache");
    headers.add("Authorization", "Basic " + Base64.getEncoder()
        .encodeToString((microserviceLogin + ":" + microservicePassword).getBytes()));
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", "password");
    map.add("username", contactLogin);
    map.add("password", contactPassword);
    map.add("DomainKey", domainKey);
    map.add("role", role);
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
    return restTemplate.postForEntity(url, entity, String.class);
  }

  public ResponseEntity<String> systemAuthenticationReload() throws HttpClientErrorException {
    log.info("systemAuthenticationReload systemUserBearerRefreshToken: {}",
        systemUserBearerRefreshToken);
    ResponseEntity<String> result = reloadTokenRequest(proxyUrl + "/oauth/token", clientLogin,
        clientPassword, systemUserBearerRefreshToken, proxyDomain, null);
    updateSystemTokens(result);
    return result;
  }

  private ResponseEntity<String> reloadTokenRequest(String url, String microserviceLogin,
      String microservicePassword, String refreshToken, String domainKey, String role)
      throws HttpClientErrorException {
    // String microserviceLogin = "af09ea17-d47c-452d-93de-2c89157b9d5b";
    // String microservicePassword = "9da73133-87f4-419e-825d-729317275f23";
    // String url = "http://localhost:8090/oauth/token";
    HttpHeaders headers = new HttpHeaders();
    // headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("Content-Type", "application/x-www-form-urlencoded");
    headers.set("accept", "*/*");
    headers.set("cache-control", "no-cache");
    headers.add("Authorization", "Basic " + Base64.getEncoder()
        .encodeToString((microserviceLogin + ":" + microservicePassword).getBytes()));
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("grant_type", "refresh_token");
    map.add("refresh_token", refreshToken);
    map.add("DomainKey", domainKey);
    if (role != null) {
      map.add("role", role);
    }
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
    ResponseEntity<String> result;
    try {
      result = restTemplate.postForEntity(proxyUrl + "/oauth/token", entity, String.class);
    } catch (HttpClientErrorException respondException) {
      log.info("respond exception: {}, body: {}", respondException.getStatusCode(),
          respondException.getResponseBodyAsString());
      if ((respondException.getStatusCode().equals(HttpStatus.UNAUTHORIZED)
          || respondException.getStatusCode().equals(HttpStatus.BAD_REQUEST))
          && refreshToken != null && refreshToken.equals(systemUserBearerRefreshToken)) {
        log.info("Yessss. fuck it!");
        return systemAuthenticationRequest();
      } else {
        throw respondException;
      }
    }
    return result;
  }

  // private String reloadProcess() {
  // reloadTokenRequest(proxyUrl + "/oauth/token", clientLogin, clientPassword,
  // systemUserBearerRefreshToken, proxyDomain, null);
  // return "start reload";
  // }

  public ResponseEntity<String> selectRequest(String bearerAccessToken, Map<String, Object> params)
      throws HttpClientErrorException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("accept", "*/*");
    headers.set("cache-control", "no-cache");
    headers.add("Authorization", "Bearer " + bearerAccessToken);
    // Map<String, Object> map = new LinkedMultiValueMap<>(params);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
    ResponseEntity<String> result;
    try {
      result = restTemplate.postForEntity(proxyUrl + "/api/request", entity, String.class);
    } catch (HttpClientErrorException respondException) {
      log.info("respond exception: {}", respondException.getMessage());
      if (respondException.getMessage().contains("401") && bearerAccessToken != null
          && bearerAccessToken.equals(systemUserBearerAccessToken)) {
        log.info("Yessss. fuck it!");
        systemAuthenticationReload();
        return selectRequest(systemUserBearerAccessToken, params);
      } else
        return new ResponseEntity("Unauthorized", HttpStatus.UNAUTHORIZED);
    }
    return result;
  }

  public ResponseEntity<String> selectCommonRequest(String bearerAccessToken,
      Map<String, Object> params) throws HttpClientErrorException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("accept", "*/*");
    headers.set("cache-control", "no-cache");
    headers.add("Authorization", "Bearer " + bearerAccessToken);
    // Map<String, Object> map = new LinkedMultiValueMap<>(params);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
    ResponseEntity<String> result;
    try {
      result = restTemplate.postForEntity(proxyUrl + "/api/request", entity, String.class);
    } catch (HttpClientErrorException respondException) {
      log.info("respond exception: {}", respondException.getMessage());
      if ((respondException.getMessage().contains("401")
          || respondException.getMessage().contains("400"))) {
        systemAuthenticationReload();
        return selectCommonRequest(systemUserBearerAccessToken, params);
      } else
        return new ResponseEntity("Unauthorized", HttpStatus.UNAUTHORIZED);
    }
    return result;
  }

  public ResponseEntity<String> collectionRequest(String bearerAccessToken,
      Map<String, Object> params) throws HttpClientErrorException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("accept", "*/*");
    headers.set("cache-control", "no-cache");
    headers.add("Authorization", "Bearer " + bearerAccessToken);
    // Map<String, Object> map = new LinkedMultiValueMap<>(params);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);
    ResponseEntity<String> result;
    try {
      result = restTemplate.postForEntity(proxyUrl + "/api/request", entity, String.class);
    } catch (HttpClientErrorException respondException) {
      log.info("respond exception: {}", respondException.getMessage());
      if (respondException.getMessage().contains("401") && bearerAccessToken != null
          && bearerAccessToken.equals(systemUserBearerAccessToken)) {
        log.info("Yessss. fuck it!");
        return systemAuthenticationReload();
      } else
        throw respondException;
    }
    return result;
  }

  private ResponseEntity<?> getErrorRespond(String message, HttpStatus status) {
    return new ResponseEntity<>(message, status);
  }

}
