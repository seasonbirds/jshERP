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

@RestController
@RequestMapping(value = "/supplierManage")
@Api(tags = {"供应商管理"})
public class SupplierManageController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(SupplierManageController.class);

    @Resource
    private SupplierManageService supplierManageService;

    @Resource
    private LogService logService;

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

    @PostMapping(value = "/add")
    @ApiOperation(value = "新增")
    public String addResource(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int insert = supplierManageService.insertSupplierManage(obj, request);
        return returnStr(objectMap, insert);
    }

    @PutMapping(value = "/update")
    @ApiOperation(value = "修改")
    public String updateResource(@RequestBody JSONObject obj, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int update = supplierManageService.updateSupplierManage(obj, request);
        return returnStr(objectMap, update);
    }

    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "删除")
    public String deleteResource(@RequestParam("id") Long id, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierManageService.deleteSupplierManage(id, request);
        return returnStr(objectMap, delete);
    }

    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "批量删除")
    public String batchDeleteResource(@RequestParam("ids") String ids, HttpServletRequest request) throws Exception {
        Map<String, Object> objectMap = new HashMap<>();
        int delete = supplierManageService.batchDeleteSupplierManage(ids, request);
        return returnStr(objectMap, delete);
    }

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
