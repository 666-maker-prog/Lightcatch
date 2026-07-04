package com.lightcatch.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MyBatisPlusConfig {
    private static final List<String> IGNORE_TABLES = Arrays.asList("sys_tenant", "ai_flow_output");

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                String tenantId = TenantContext.getCurrentTenantId();
                return new StringValue(tenantId != null ? tenantId : "0");
            }

            @Override
            public String getTenantIdColumn() { return "tenant_id"; }

            @Override
            public boolean ignoreTable(String tableName) {
                return IGNORE_TABLES.contains(tableName);
            }
        }));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }

    public static class TenantContext {
        private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
        public static void setCurrentTenantId(String tenantId) { CURRENT_TENANT.set(tenantId); }
        public static String getCurrentTenantId() { return CURRENT_TENANT.get(); }
        public static void clear() { CURRENT_TENANT.remove(); }
    }
}
