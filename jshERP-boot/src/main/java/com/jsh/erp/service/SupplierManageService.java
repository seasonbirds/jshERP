package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.SupplierManage;
import com.jsh.erp.datasource.entities.SupplierManageExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.SupplierManageMapper;
import com.jsh.erp.datasource.mappers.SupplierManageMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import com.jsh.erp.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SupplierManageService {
    private Logger logger = LoggerFactory.getLogger(SupplierManageService.class);

    @Resource
    private SupplierManageMapper supplierManageMapper;

    @Resource
    private SupplierManageMapperEx supplierManageMapperEx;

    @Resource
    private LogService logService;

    @Resource
    private UserService userService;

    public SupplierManage getSupplierManage(long id) throws Exception {
        SupplierManage result = null;
        try {
            result = supplierManageMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<SupplierManage> getSupplierManageListByIds(String ids) throws Exception {
        List<Long> idList = StringUtil.strToLongList(ids);
        List<SupplierManage> list = new ArrayList<>();
        try {
            SupplierManageExample example = new SupplierManageExample();
            example.createCriteria().andIdIn(idList);
            list = supplierManageMapper.selectByExample(example);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<SupplierManage> getSupplierManage() throws Exception {
        SupplierManageExample example = new SupplierManageExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<SupplierManage> list = null;
        try {
            list = supplierManageMapper.selectByExample(example);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<SupplierManage> select(String supplierName, String creditCode, String beginTime, String endTime, String status) throws Exception {
        List<SupplierManage> list = new ArrayList<>();
        try {
            PageUtils.startPage();
            list = supplierManageMapperEx.selectByConditionSupplierManage(supplierName, creditCode, beginTime, endTime, status);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertSupplierManage(JSONObject obj, HttpServletRequest request) throws Exception {
        SupplierManage supplierManage = JSONObject.parseObject(obj.toJSONString(), SupplierManage.class);
        int result = 0;
        try {
            supplierManage.setStatus("0");
            supplierManage.setCreateTime(new Date());
            supplierManage.setUpdateTime(new Date());
            User userInfo = userService.getCurrentUser();
            supplierManage.setCreator(userInfo == null ? null : userInfo.getId());
            supplierManage.setTenantId(userInfo == null ? null : userInfo.getTenantId());
            supplierManage.setDeleteFlag(BusinessConstants.DELETE_FLAG_EXISTS);
            result = supplierManageMapper.insertSelective(supplierManage);
            logService.insertLog("供应商管理",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ADD).append(supplierManage.getSupplierName()).toString(), request);
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateSupplierManage(JSONObject obj, HttpServletRequest request) throws Exception {
        SupplierManage supplierManage = JSONObject.parseObject(obj.toJSONString(), SupplierManage.class);
        int result = 0;
        try {
            supplierManage.setUpdateTime(new Date());
            result = supplierManageMapper.updateByPrimaryKeySelective(supplierManage);
            logService.insertLog("供应商管理",
                    new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_EDIT).append(supplierManage.getSupplierName()).toString(), request);
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSupplierManage(Long id, HttpServletRequest request) throws Exception {
        return batchDeleteSupplierManageByIds(id.toString());
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSupplierManage(String ids, HttpServletRequest request) throws Exception {
        return batchDeleteSupplierManageByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSupplierManageByIds(String ids) throws Exception {
        int result = 0;
        String[] idArray = ids.split(",");
        StringBuffer sb = new StringBuffer();
        sb.append(BusinessConstants.LOG_OPERATION_TYPE_DELETE);
        List<SupplierManage> list = getSupplierManageListByIds(ids);
        for (SupplierManage supplierManage : list) {
            sb.append("[").append(supplierManage.getSupplierName()).append("]");
        }
        logService.insertLog("供应商管理", sb.toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        User userInfo = userService.getCurrentUser();
        try {
            result = supplierManageMapperEx.batchDeleteSupplierManageByIds(new Date(), userInfo == null ? null : userInfo.getId(), idArray);
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name) throws Exception {
        SupplierManageExample example = new SupplierManageExample();
        example.createCriteria().andIdNotEqualTo(id).andSupplierNameEqualTo(name).andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<SupplierManage> list = null;
        try {
            list = supplierManageMapper.selectByExample(example);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list == null ? 0 : list.size();
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchSetStatus(String status, String ids) throws Exception {
        logService.insertLog("供应商管理",
                new StringBuffer(BusinessConstants.LOG_OPERATION_TYPE_ENABLED).toString(),
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        String[] idArray = ids.split(",");
        int result = 0;
        try {
            result = supplierManageMapperEx.batchSetStatus(status, idArray);
        } catch (Exception e) {
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public List<SupplierManage> findByAll(String supplierName, String creditCode, String beginTime, String endTime, String status) throws Exception {
        List<SupplierManage> list = null;
        try {
            list = supplierManageMapperEx.findByAll(supplierName, creditCode, beginTime, endTime, status);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list;
    }

    public File exportExcel(List<SupplierManage> dataList) throws Exception {
        String[] names = {"供应商名称", "信用代码", "注册地址", "经营地址", "法人", "联系电话",
                "注册资本", "资质", "入驻时间", "状态", "备注"};
        String title = "供应商管理信息";
        List<Object[]> objects = new ArrayList<>();
        if (null != dataList) {
            for (SupplierManage s : dataList) {
                Object[] objs = new Object[names.length];
                objs[0] = s.getSupplierName();
                objs[1] = s.getCreditCode();
                objs[2] = s.getRegisterAddress();
                objs[3] = s.getBusinessAddress();
                objs[4] = s.getLegalPerson();
                objs[5] = s.getContactPhone();
                objs[6] = s.getRegisteredCapital();
                objs[7] = s.getQualification();
                objs[8] = s.getEntryTime();
                objs[9] = getStatusText(s.getStatus());
                objs[10] = s.getRemark();
                objects.add(objs);
            }
        }
        return com.jsh.erp.utils.ExcelUtils.exportObjectsOneSheet(title, "*导入时本行内容请勿删除，切记！", names, title, objects);
    }

    private String getStatusText(String status) {
        if ("0".equals(status)) {
            return "待审核";
        } else if ("1".equals(status)) {
            return "已生效";
        } else if ("2".equals(status)) {
            return "已拒绝";
        }
        return status;
    }
}
