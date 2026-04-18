package com.jsh.erp.controller;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.base.BaseController;
import com.jsh.erp.base.TableDataInfo;
import com.jsh.erp.datasource.entities.SupplierManage;
import com.jsh.erp.service.LogService;
import com.jsh.erp.service.SupplierManageService;
import com.jsh.erp.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jsh.erp.utils.ResponseJsonUtil.returnJson;
import static com.jsh.erp.utils.ResponseJsonUtil.returnStr;

/**
 * 供应商管理控制器
 * 提供供应商管理的REST API接口，包括增删改查、审核、导出等功能
 * 
 * 供应商状态说明：
 * 0-待审核：新创建的供应商，需要审核后才能生效
 * 1-已生效：审核通过，可在业务中使用
 * 2-已拒绝：审核不通过
 */
@RestController
@RequestMapping(value = "/supplierManage")
@Api(tags = {"供应商管理"})
public class SupplierManageController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(SupplierManageController.class);

    @Resource
    private SupplierManageService supplierManageService;

    @Resource
    private LogService logService;

    /**
     * 根据ID获取供应商详情
     * @param id 供应商ID
     * @param request HTTP请求对象
     * @return 供应商详情JSON
     * @throws Exception 异常
     */
    @GetMapping(value = "/info")
    @ApiOperation(value = "根据id获取信息")
    public String getList(@RequestParam("id") Long id,
                          HttpServletRequest request) throws Exception {
        SupplierManage supplierManage = supplierManageService.getSupplierManage(id);
        Map<String, Object> objectMap = new HashMap<>();
        if (supplierManage != null) {
            objectMap.put("info", supplierManage);
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 获取供应商列表（带分页和查询条件）
     * 支持的查询条件：
     * - supplierName：供应商名称（模糊查询）
     * - creditCode：信用代码（模糊查询）
     * - beginTime/endTime：入驻时间范围
     * - status：状态（0-待审核，1-已生效，2-已拒绝）
     * 
     * @param search 查询条件JSON字符串
     * @param request HTTP请求对象
     * @return 分页数据列表
     * @throws Exception 异常
     */
    @GetMapping(value = "/list")
    @ApiOperation(value = "获取信息列表")
    public TableDataInfo getList(@RequestParam(value = Constants.SEARCH, required = false) String search,
                                 HttpServletRequest request) throws Exception {
        String supplierName = StringUtil.getInfo(search, "supplierName");
        String creditCode = StringUtil.getInfo(search, "creditCode");
        String beginTime = StringUtil.getInfo(search, "beginTime");
        String endTime = StringUtil.getInfo(search, "endTime");
        String status = StringUtil.getInfo(search, "status");
        List<SupplierManage> list = supplierManageService.select(supplierName, creditCode, beginTime, endTime, status);
        return getDataTable(list);
    }

    /**
     * 新增供应商
     * 新创建的供应商状态默认为"待审核"(0)
     * 
     * @param obj 供应商信息JSON对象
     * @param request HTTP请求对象
     * @return 操作结果
     * @throws Exception 异常
     */
    @PostMapping(value = "/add")
    @ApiOperation(value = "新增")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = supplierManageService.insertSupplierManage(obj, request);
        return returnStr(objectMap, insert);
    }

    /**
     * 修改供应商信息
     * 
     * @param obj 供应商信息JSON对象
     * @param request HTTP请求对象
     * @return 操作结果
     * @throws Exception 异常
     */
    @PutMapping(value = "/update")
    @ApiOperation(value = "修改")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = supplierManageService.updateSupplierManage(obj, request);
        return returnStr(objectMap, update);
    }

    /**
     * 删除单个供应商（逻辑删除）
     * 
     * @param id 供应商ID
     * @param request HTTP请求对象
     * @return 操作结果
     * @throws Exception 异常
     */
    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "删除")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierManageService.deleteSupplierManage(id, request);
        return returnStr(objectMap, delete);
    }

    /**
     * 批量删除供应商（逻辑删除）
     * 
     * @param ids 供应商ID，多个用逗号分隔
     * @param request HTTP请求对象
     * @return 操作结果
     * @throws Exception 异常
     */
    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "批量删除")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierManageService.batchDeleteSupplierManage(ids, request);
        return returnStr(objectMap, delete);
    }

    /**
     * 检查供应商名称是否已存在
     * 用于新增/编辑时的名称唯一性校验
     * 
     * @param id 当前供应商ID（编辑时传入，新增时传入0）
     * @param name 供应商名称
     * @param request HTTP请求对象
     * @return status=true表示已存在，false表示不存在
     * @throws Exception 异常
     */
    @GetMapping(value = "/checkIsNameExist")
    @ApiOperation(value = "检查名称是否存在")
    public String checkIsNameExist(@RequestParam Long id, @RequestParam(value = "name", required = false) String name,
                                   HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int exist = supplierManageService.checkIsNameExist(id, name);
        if (exist > 0) {
            objectMap.put("status", true);
        } else {
            objectMap.put("status", false);
        }
        return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
    }

    /**
     * 批量设置供应商状态（审核功能）
     * 状态说明：
     * - 设为"1"：审核通过，状态变为"已生效"
     * - 设为"2"：审核拒绝，状态变为"已拒绝"
     * - 设为"0"：反审核（仅管理员）
     * 
     * @param jsonObject 包含status和ids的JSON对象
     * @param request HTTP请求对象
     * @return 操作结果
     * @throws Exception 异常
     */
    @PostMapping(value = "/batchSetStatus")
    @ApiOperation(value = "批量设置状态-审核或者反审核")
    public String batchSetStatus(@RequestBody JSONObject jsonObject,
                                 HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        String status = jsonObject.getString("status");
        String ids = jsonObject.getString("ids");
        int res = supplierManageService.batchSetStatus(status, ids);
        if (res > 0) {
            return returnJson(objectMap, ErpInfo.OK.name, ErpInfo.OK.code);
        } else {
            return returnJson(objectMap, ErpInfo.ERROR.name, ErpInfo.ERROR.code);
        }
    }

    /**
     * 导出供应商列表到Excel
     * 支持按查询条件筛选导出
     * 
     * @param supplierName 供应商名称
     * @param creditCode 信用代码
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     */
    @GetMapping(value = "/exportExcel")
    public void exportExcel(@RequestParam(value = "supplierName", required = false) String supplierName,
                            @RequestParam(value = "creditCode", required = false) String creditCode,
                            @RequestParam(value = "beginTime", required = false) String beginTime,
                            @RequestParam(value = "endTime", required = false) String endTime,
                            @RequestParam(value = "status", required = false) String status,
                            HttpServletRequest request, HttpServletResponse response) {
        try {
            List<SupplierManage> dataList = supplierManageService.findByAll(supplierName, creditCode, beginTime, endTime, status);
            File file = supplierManageService.exportExcel(dataList);
            ExcelUtils.downloadExcel(file, file.getName(), response);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
