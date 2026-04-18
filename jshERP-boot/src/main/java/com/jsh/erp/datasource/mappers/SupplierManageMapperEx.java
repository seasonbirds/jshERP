package com.jsh.erp.datasource.mappers;

import com.jsh.erp.datasource.entities.SupplierManage;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SupplierManageMapperEx {

    List<SupplierManage> selectByConditionSupplierManage(
            @Param("supplierName") String supplierName,
            @Param("creditCode") String creditCode,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("status") String status);

    List<SupplierManage> findByAll(
            @Param("supplierName") String supplierName,
            @Param("creditCode") String creditCode,
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("status") String status);

    int batchDeleteSupplierManageByIds(@Param("updateTime") Date updateTime, @Param("updater") Long updater, @Param("ids") String ids[]);

    int batchSetStatus(@Param("status") String status, @Param("ids") String ids[]);

    SupplierManage getSupplierManageByName(
            @Param("supplierName") String supplierName);
}
