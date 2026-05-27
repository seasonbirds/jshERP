package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.Log;
import com.jsh.erp.datasource.entities.LogExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.LogMapper;
import com.jsh.erp.datasource.mappers.LogMapperEx;
import com.jsh.erp.datasource.vo.LogVo4List;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import com.jsh.erp.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.jsh.erp.utils.Tools.getLocalIp;

@Service
public class LogService {
    private Logger logger = LoggerFactory.getLogger(LogService.class);
    @Resource
    private LogMapper logMapper;

    @Resource
    private LogMapperEx logMapperEx;

    @Resource
    private UserService userService;

    public Log getLog(long id)throws Exception {
        Log result=null;
        try{
            result=logMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<Log> getLog()throws Exception {
        LogExample example = new LogExample();
        List<Log> list=null;
        try{
            list=logMapper.selectByExample(example);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<LogVo4List> select(String operation, String userInfo, String clientIp, String tenantLoginName, String tenantType,
                                   String beginTime, String endTime, String content)throws Exception {
        List<LogVo4List> list=null;
        try{
            beginTime = Tools.parseDayToTime(beginTime,BusinessConstants.DAY_FIRST_TIME);
            endTime = Tools.parseDayToTime(endTime,BusinessConstants.DAY_LAST_TIME);
            PageUtils.startPage();
            list=logMapperEx.selectByConditionLog(operation, userInfo, clientIp, tenantLoginName, tenantType, beginTime, endTime,
                    content);
            if (null != list) {
                for (LogVo4List log : list) {
                    log.setCreateTimeStr(Tools.getCenternTime(log.getCreateTime()));
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertLog(JSONObject obj, HttpServletRequest request) throws Exception{
        Log log = JSONObject.parseObject(obj.toJSONString(), Log.class);
        int result=0;
        try{
            result=logMapper.insertSelective(log);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateLog(JSONObject obj, HttpServletRequest request)throws Exception {
        Log log = JSONObject.parseObject(obj.toJSONString(), Log.class);
        int result=0;
        try{
            result=logMapper.updateByPrimaryKeySelective(log);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteLog(Long id, HttpServletRequest request)throws Exception {
        int result=0;
        try{
            result=logMapper.deleteByPrimaryKey(id);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteLog(String ids, HttpServletRequest request)throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        LogExample example = new LogExample();
        example.createCriteria().andIdIn(idList);
        int result=0;
        try{
            result=logMapper.deleteByExample(example);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    /**
     * 供AOP切面调用的审计日志写入方法。
     * 自动从请求上下文中获取当前用户ID和租户ID，获取客户端IP，写入审计日志。
     * 使用insertLogWithUserId绕过租户过滤插件，直接写入日志记录。
     *
     * @param moduleName 模块名称（对应Log表的operation字段）
     * @param content    日志内容
     * @param request    HTTP请求
     */
    public void createAuditLog(String moduleName, String content, HttpServletRequest request) {
        try {
            Long userId = userService.getUserId(request);
            if (userId != null) {
                Long tenantId = null;
                try {
                    User currentUser = userService.getCurrentUser();
                    if (currentUser != null) {
                        tenantId = currentUser.getTenantId();
                    }
                } catch (Exception e) {
                    // 获取当前用户失败时tenantId保持null，不影响日志记录
                }
                createAuditLog(userId, tenantId, moduleName, content, request);
            }
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
    }

    /**
     * 供AOP切面调用的审计日志写入方法（指定userId和tenantId）。
     * 用于登录等场景：用户尚未登录到session，无法从session获取userId，需要显式传入。
     * 使用insertLogWithUserId绕过租户过滤插件，直接写入日志记录。
     *
     * @param userId     用户ID
     * @param tenantId   租户ID（可为null）
     * @param moduleName 模块名称（对应Log表的operation字段）
     * @param content    日志内容
     * @param request    HTTP请求
     */
    public void createAuditLog(Long userId, Long tenantId, String moduleName, String content, HttpServletRequest request) {
        try {
            if (userId != null) {
                Log log = new Log();
                log.setUserId(userId);
                log.setOperation(moduleName);
                log.setClientIp(getLocalIp(request));
                log.setCreateTime(new Date());
                log.setStatus((byte) 0);
                log.setContent(content);
                log.setTenantId(tenantId);
                logMapperEx.insertLogWithUserId(log);
            }
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
    }
}
