package com.jsh.erp.service;

import com.alibaba.fastjson.JSONObject;
import com.jsh.erp.annotation.AuditLog;
import com.jsh.erp.aop.AuditLogContext;
import com.jsh.erp.constants.BusinessConstants;
import com.jsh.erp.datasource.entities.MaterialProperty;
import com.jsh.erp.datasource.entities.MaterialPropertyExample;
import com.jsh.erp.datasource.entities.User;
import com.jsh.erp.datasource.mappers.MaterialPropertyMapper;
import com.jsh.erp.datasource.mappers.MaterialPropertyMapperEx;
import com.jsh.erp.exception.JshException;
import com.jsh.erp.utils.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class MaterialPropertyService {
    private Logger logger = LoggerFactory.getLogger(MaterialPropertyService.class);

    @Resource
    private MaterialPropertyMapper materialPropertyMapper;

    @Resource
    private MaterialPropertyMapperEx materialPropertyMapperEx;
    @Resource
    private UserService userService;

    public MaterialProperty getMaterialProperty(long id)throws Exception {
        MaterialProperty result=null;
        try{
            result=materialPropertyMapper.selectByPrimaryKey(id);
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return result;
    }

    public List<MaterialProperty> getMaterialProperty()throws Exception {
        MaterialPropertyExample example = new MaterialPropertyExample();
        example.createCriteria().andDeleteFlagNotEqualTo(BusinessConstants.DELETE_FLAG_DELETED);
        List<MaterialProperty> list = new ArrayList<>();
        try{
            buildMaterialProperty(list);
            List<MaterialProperty> mpList = materialPropertyMapper.selectByExample(example);
            Map<String, String> mpMap = new HashMap<>();
            for(MaterialProperty mp: mpList) {
                mpMap.put(mp.getNativeName(), mp.getAnotherName());
            }
            //给list里面的别名和排序做更新
            for(MaterialProperty item: list) {
                if(mpMap.get(item.getNativeName())!=null) {
                    item.setAnotherName(mpMap.get(item.getNativeName()));
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    public List<MaterialProperty> select(String name)throws Exception {
        List<MaterialProperty> list = new ArrayList<>();
        try{
            buildMaterialProperty(list);
            PageUtils.startPage();
            List<MaterialProperty> mpList = materialPropertyMapperEx.selectByConditionMaterialProperty(name);
            Map<String, String> mpMap = new HashMap<>();
            for(MaterialProperty mp: mpList) {
                mpMap.put(mp.getNativeName(), mp.getAnotherName());
            }
            //给list里面的别名和排序做更新
            for(MaterialProperty item: list) {
                if(mpMap.get(item.getNativeName())!=null) {
                    item.setAnotherName(mpMap.get(item.getNativeName()));
                }
            }
        }catch(Exception e){
            JshException.readFail(logger, e);
        }
        return list;
    }

    private void buildMaterialProperty(List<MaterialProperty> list) {
        MaterialProperty mp1 = new MaterialProperty();
        MaterialProperty mp2 = new MaterialProperty();
        MaterialProperty mp3 = new MaterialProperty();
        mp1.setId(1L);
        mp1.setNativeName("扩展1");
        mp1.setAnotherName("扩展1");
        list.add(mp1);
        mp2.setId(2L);
        mp2.setNativeName("扩展2");
        mp2.setAnotherName("扩展2");
        list.add(mp2);
        mp3.setId(3L);
        mp3.setNativeName("扩展3");
        mp3.setAnotherName("扩展3");
        list.add(mp3);
    }

    @AuditLog(moduleName="商品属性", operationType="")
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int insertMaterialProperty(JSONObject obj, HttpServletRequest request)throws Exception {
        MaterialProperty materialProperty = JSONObject.parseObject(obj.toJSONString(), MaterialProperty.class);
        int  result=0;
        try{
            result = materialPropertyMapper.insertSelective(materialProperty);
            AuditLogContext.setContent(BusinessConstants.LOG_OPERATION_TYPE_ADD + materialProperty.getNativeName());
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @AuditLog(moduleName="商品属性", operationType="")
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int updateMaterialProperty(JSONObject obj, HttpServletRequest request)throws Exception {
        MaterialProperty materialProperty = JSONObject.parseObject(obj.toJSONString(), MaterialProperty.class);
        int  result=0;
        try{
            result = materialPropertyMapper.updateByPrimaryKeySelective(materialProperty);
            AuditLogContext.setContent(BusinessConstants.LOG_OPERATION_TYPE_EDIT + materialProperty.getNativeName());
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    @AuditLog(moduleName="商品属性", operationType="")
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int deleteMaterialProperty(Long id, HttpServletRequest request)throws Exception {
        return batchDeleteMaterialPropertyByIds(id.toString());
    }

    @AuditLog(moduleName="商品属性", operationType="")
    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteMaterialProperty(String ids, HttpServletRequest request)throws Exception {
        return batchDeleteMaterialPropertyByIds(ids);
    }

    @Transactional(value = "transactionManager", rollbackFor = Exception.class)
    public int batchDeleteMaterialPropertyByIds(String ids) throws Exception{
        User userInfo=userService.getCurrentUser();
        String [] idArray=ids.split(",");
        int  result=0;
        try{
            result = materialPropertyMapperEx.batchDeleteMaterialPropertyByIds(new Date(), userInfo == null ? null : userInfo.getId(), idArray);
            AuditLogContext.setContent(BusinessConstants.LOG_OPERATION_TYPE_DELETE + ids);
        }catch(Exception e){
            JshException.writeFail(logger, e);
        }
        return result;
    }

    public int checkIsNameExist(Long id, String name)throws Exception {
        return 0;
    }

    public boolean checkIsNativeNameExist(String nativeName) {
        int count = materialPropertyMapperEx.getCountByNativeName(nativeName);
        return count>0;
    }

    public int updateMaterialPropertyByNativeName(String nativeName, String anotherName) {
        materialPropertyMapperEx.updateMaterialPropertyByNativeName(nativeName, anotherName);
        return 1;
    }
}
