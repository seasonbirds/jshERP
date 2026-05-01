package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.SupplierManage;
import com.jsh.erp.datasource.entities.SupplierManageExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SupplierManageMapper {
    long countByExample(SupplierManageExample example);

    int deleteByExample(SupplierManageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SupplierManage record);

    int insertSelective(SupplierManage record);

    List<SupplierManage> selectByExample(SupplierManageExample example);

    SupplierManage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SupplierManage record, @Param("example") SupplierManageExample example);

    int updateByExample(@Param("record") SupplierManage record, @Param("example") SupplierManageExample example);

    int updateByPrimaryKeySelective(SupplierManage record);

    int updateByPrimaryKey(SupplierManage record);
}
