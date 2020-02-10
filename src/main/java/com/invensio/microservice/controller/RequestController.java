package com.invensio.microservice.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.invensio.microservice.service.AuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@RestController
@RequestMapping("/")
public class RequestController {

  @Autowired
  RestTemplate restTemplate;

  AuthorizationService authorizationService;

  @Autowired
  public void setAuthorizationService(AuthorizationService authorizationService) {
    this.authorizationService = authorizationService;
  }

  private ObjectMapper objectMapper;

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // @RequestMapping(value = "/", method = RequestMethod.GET)
  // public ResponseEntity<?> test() {
  // // String url = "http://localhost:8090";
  // // String result = this.restTemplate.postForEntity(url, String.class);
  // // log.info("restTemplate result: {}", createPost());
  // // log.info("authentication test: {}", authenticationRequest());
  // authorizationService.systemAuthenticationRequest();
  // // Map<String, Object> values = new HashMap<>();
  // // values.put("data",
  // // "{\"DOMAINKEY\":\"liteconstruct.com\",\"CCLASS\":\"7a38bfb3-7874-4eb4-b981-b38e5ade2df8\",
  // // \"METHOD\":\"SELECT\", \"ROLE\":\"1d021b86-41c6-47c1-a38e-0aa89b98dc28\"}");
  // // return new ResponseEntity<>(authorizationService.selectRequest(null, values),
  // HttpStatus.OK);
  // return new ResponseEntity<>("success", HttpStatus.OK);
  // }

  // @RequestMapping(value = "/check", method = RequestMethod.GET)
  // public ResponseEntity<?> check() {
  // Map<String, Object> values = new HashMap<>();
  // try {
  // values = objectMapper.readValue(
  // "{\"DOMAINKEY\":\"liteconstruct.com\",\"CCLASS\":\"7a38bfb3-7874-4eb4-b981-b38e5ade2df8\",\"METHOD\":\"SELECT\",
  // \"METHODSPECIAL\":\"COLLECTION\"}",
  // new TypeReference<Map<String, Object>>() {});
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  //
  // return new ResponseEntity<>(authorizationService
  // .selectRequest(authorizationService.systemUserBearerAccessToken, values).getBody(),
  // HttpStatus.OK);
  // // return new ResponseEntity<>(
  // // "systemUserBearerAccessToken:" + authorizationService.systemUserBearerAccessToken
  // // + ", systemUserBearerRefreshToken:" + authorizationService.systemUserBearerRefreshToken,
  // // HttpStatus.OK);
  // }

  // @CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "false")
  // @RequestMapping(value = "/login", method = RequestMethod.POST)
  // public ResponseEntity<?> login(@RequestBody Map<String, Object> requestBody) {
  // log.info("requestBody: {}", requestBody);
  // return authorizationService.systemAuthenticationRequest(requestBody);
  // }

  // @CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "false")
  // @RequestMapping(value = "/request", method = RequestMethod.POST)
  // public ResponseEntity<?> getCollection(@RequestHeader("Authorization") String authBearer,
  // @RequestBody Map<String, Object> requestBody) {
  // log.info("requestBody: {}", requestBody);
  // return authorizationService.selectRequest(authBearer.substring(7).trim(), requestBody);
  // }

  @CrossOrigin(origins = "*", maxAge = 3600, allowCredentials = "false")
  @RequestMapping(value = "/common/request", method = RequestMethod.POST)
  public ResponseEntity<?> getCommonRequest(@RequestBody Map<String, Object> requestBody) {
    log.info("token: {}, requestBody: {}", AuthorizationService.systemUserBearerAccessToken,
        requestBody);
    if (AuthorizationService.systemUserBearerAccessToken == null) {
      authorizationService.systemAuthenticationRequest();
    }
    return authorizationService
        .selectCommonRequest(AuthorizationService.systemUserBearerAccessToken, requestBody);
  }

  // @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
  // public ResponseEntity<?> loginOptions(@RequestBody Map<String, Object> requestBody) {
  // log.info("request for options");
  // return authorizationService.systemAuthenticationRequest(requestBody);
  // }
}
