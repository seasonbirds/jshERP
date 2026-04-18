package com.jsh.erp.aop.entity;

import java.io.Serializable;

public class ApiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private String requestTime;

    private String url;

    private String ip;

    private String userAgent;

    private String userId;

    private String requestParams;

    private String responseParams;

    private Long costTime;

    private Integer responseStatus;

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(String responseParams) {
        this.responseParams = responseParams;
    }

    public Long getCostTime() {
        return costTime;
    }

    public void setCostTime(Long costTime) {
        this.costTime = costTime;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    @Override
    public String toString() {
        return "ApiLog{" +
                "requestTime='" + requestTime + '\'' +
                ", url='" + url + '\'' +
                ", ip='" + ip + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", userId='" + userId + '\'' +
                ", requestParams='" + requestParams + '\'' +
                ", responseParams='" + responseParams + '\'' +
                ", costTime=" + costTime +
                ", responseStatus=" + responseStatus +
                '}';
    }
}
