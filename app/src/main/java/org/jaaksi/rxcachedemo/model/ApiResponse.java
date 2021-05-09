package org.jaaksi.rxcachedemo.model;

public class ApiResponse<T> {
  public int errorCode;
  public String errorMsg;
  public T data;

  public boolean isSuccess() {
    return errorCode == 0;
  }

  public boolean hasData() {
    return isSuccess() && data != null;
  }
}
