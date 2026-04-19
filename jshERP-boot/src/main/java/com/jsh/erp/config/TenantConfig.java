package com.jsh.erp.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.jsh.erp.utils.Tools;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TenantConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(HttpServletRequest request) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
        tenantInterceptor.setTenantLineHandler(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String token = request.getHeader("X-Access-Token");
                Long tenantId = Tools.getTenantIdByToken(token);
                if (tenantId != 0L) {
                    return new LongValue(tenantId);
                } else {
                    return null;
                }
            }

            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }

            @Override
            public boolean ignoreTable(String tableName) {
                Boolean res = true;
                String token = request.getHeader("X-Access-Token");
                Long tenantId = Tools.getTenantIdByToken(token);
                if (tenantId != 0L) {
                    if ("jsh_sequence".equals(tableName) || "jsh_function".equals(tableName)
                            || "jsh_platform_config".equals(tableName) || "jsh_tenant".equals(tableName)) {
                        res = true;
                    } else {
                        res = false;
                    }
                }
                return res;
            }
        });
        interceptor.addInnerInterceptor(tenantInterceptor);
        
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        
        return interceptor;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
        scannerConfigurer.setBasePackage("com.jsh.erp.datasource.mappers*");
        return scannerConfigurer;
    }

}
