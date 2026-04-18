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

/**
 * 供应商管理服务类
 * 实现供应商管理的业务逻辑，包括增删改查、审核、导出等核心功能
 * 
 * 供应商状态流转：
 * 待审核(0) -> 已生效(1) / 已拒绝(2)
 * 已生效(1) -> 反审核 -> 待审核(0) （仅管理员可操作）
 */
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

    /**
     * 根据ID获取供应商详情
     * @param id 供应商主键ID
     * @return 供应商实体对象，不存在返回null
     * @throws Exception 异常
     */
    public SupplierManage getSupplierManage(long id) throws Exception {
        SupplierManage result = null;
        try {
            result = supplierManageMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return result;
    }

    /**
     * 根据ID列表批量获取供应商
     * @param ids 供应商ID字符串，多个用逗号分隔
     * @return 供应商列表
     * @throws Exception 异常
     */
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

    /**
     * 获取所有未删除的供应商列表
     * @return 供应商列表
     * @throws Exception 异常
     */
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

    /**
     * 分页查询供应商列表
     * 支持多条件组合查询：
     * - 名称模糊查询
     * - 信用代码模糊查询
     * - 入驻时间范围查询
     * - 状态精确查询
     * 
     * @param supplierName 供应商名称（模糊）
     * @param creditCode 信用代码（模糊）
     * @param beginTime 入驻开始时间
     * @param endTime 入驻结束时间
     * @param status 状态
     * @return 分页后的供应商列表
     * @throws Exception 异常
     */
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

    /**
     * 新增供应商
     * 业务逻辑：
     * 1. 新供应商状态默认为"待审核"(0)
     * 2. 自动设置创建时间、更新时间
     * 3. 设置创建人、租户ID
     * 4. 记录操作日志
     * 
     * @param obj 供应商信息JSON对象
     * @param request HTTP请求对象
     * @return 插入的记录数
     * @throws Exception 异常
     */
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

    /**
     * 修改供应商信息
     * 业务逻辑：
     * 1. 自动更新修改时间
     * 2. 记录操作日志
     * 
     * @param obj 供应商信息JSON对象
     * @param request HTTP请求对象
     * @return 更新的记录数
     * @throws Exception 异常
     */
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

    /**
     * 删除单个供应商
     * @param id 供应商ID
     * @param request HTTP请求对象
     * @return 删除的记录数
     * @throws Exception 异常
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteSupplierManage(Long id, HttpServletRequest request) throws Exception {
        return batchDeleteSupplierManageByIds(id.toString());
    }

    /**
     * 批量删除供应商
     * @param ids 供应商ID字符串，多个用逗号分隔
     * @param request HTTP请求对象
     * @return 删除的记录数
     * @throws Exception 异常
     */
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteSupplierManage(String ids, HttpServletRequest request) throws Exception {
        return batchDeleteSupplierManageByIds(ids);
    }

    /**
     * 批量删除供应商（逻辑删除）
     * 业务逻辑：
     * 1. 将delete_flag标记为已删除
     * 2. 记录操作人、操作时间
     * 3. 记录操作日志
     * 
     * @param ids 供应商ID字符串，多个用逗号分隔
     * @return 删除的记录数
     * @throws Exception 异常
     */
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

    /**
     * 检查供应商名称是否已存在
     * 用于表单验证，防止重复名称
     * 
     * @param id 当前供应商ID（编辑时排除自身）
     * @param name 供应商名称
     * @return 存在的数量，大于0表示已存在
     * @throws Exception 异常
     */
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

    /**
     * 批量设置供应商状态（审核功能）
     * 业务逻辑：
     * - 状态"1"：审核通过，供应商变为"已生效"
     * - 状态"2"：审核拒绝，供应商变为"已拒绝"
     * - 状态"0"：反审核，供应商变为"待审核"（仅管理员可操作）
     * 
     * @param status 目标状态
     * @param ids 供应商ID字符串，多个用逗号分隔
     * @return 更新的记录数
     * @throws Exception 异常
     */
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

    /**
     * 查询所有符合条件的供应商（不分页，用于导出）
     * @param supplierName 供应商名称
     * @param creditCode 信用代码
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @return 供应商列表
     * @throws Exception 异常
     */
    public List<SupplierManage> findByAll(String supplierName, String creditCode, String beginTime, String endTime, String status) throws Exception {
        List<SupplierManage> list = null;
        try {
            list = supplierManageMapperEx.findByAll(supplierName, creditCode, beginTime, endTime, status);
        } catch (Exception e) {
            JshException.readFail(logger, e);
        }
        return list;
    }

    /**
     * 导出供应商数据到Excel
     * 导出字段：供应商名称、信用代码、注册地址、经营地址、法人、联系电话、
     *           注册资本、资质、入驻时间、状态、备注
     * 
     * @param dataList 要导出的供应商数据列表
     * @return Excel文件对象
     * @throws Exception 异常
     */
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

    /**
     * 将状态代码转换为中文描述
     * @param status 状态代码
     * @return 状态中文描述
     */
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
