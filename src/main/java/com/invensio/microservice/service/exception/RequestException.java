package com.invensio.microservice.service.exception;

public class RequestException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 316065196310701309L;

  public RequestException(String errorMessage) {
    super(errorMessage);
  }
}
