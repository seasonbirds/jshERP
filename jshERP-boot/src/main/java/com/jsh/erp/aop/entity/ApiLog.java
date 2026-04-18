package com.jsh.erp.aop.entity;

import java.io.Serializable;

/**
 * API接口日志实体类
 * 
 * 用于存储外部接口请求的详细信息，包括：
 * - 请求基本信息（时间、URL、IP、User-Agent、用户）
 * - 请求参数（文件类型忽略，超过200K截断）
 * - 响应参数（文件类型忽略，超过500K截断）
 * - 执行耗时和响应状态
 * 
 * 所有敏感信息会在采集时进行脱敏处理
 * 
 * @author jshERP
 */
public class ApiLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求时间，格式：yyyy-MM-dd HH:mm:ss.SSS
     */
    private String requestTime;

    /**
     * 请求URL（URI路径）
     */
    private String url;

    /**
     * 客户端IP地址（支持代理服务器）
     */
    private String ip;

    /**
     * 客户端User-Agent信息
     */
    private String userAgent;

    /**
     * 当前用户ID，未登录则为空
     */
    private String userId;

    /**
     * 请求参数（JSON格式）
     * 注意：文件类型参数忽略，超过200K截断，敏感信息已脱敏
     */
    private String requestParams;

    /**
     * 响应参数（JSON格式）
     * 注意：文件类型响应标记为[FILE_RESPONSE]，超过500K截断，敏感信息已脱敏
     */
    private String responseParams;

    /**
     * 接口执行耗时（毫秒）
     */
    private Long costTime;

    /**
     * 响应状态
     * 1 - 请求处理成功
     * 非1 - 请求失败，具体为接口返回的code值，无返回值设为0
     */
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
