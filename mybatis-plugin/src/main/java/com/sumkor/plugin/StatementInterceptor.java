package com.sumkor.plugin;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;

import java.sql.Connection;

/**
 * 拦截 StatementHandler#prepare 方法
 *
 * @author Sumkor
 * @since 2021/7/26
 */
@Intercepts({
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
})
@Slf4j
public class StatementInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.info("------------------StatementInterceptor#intercept 开始------------------");
        // 获取当前拦截的目标类
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        // 通过反射工具类，获取 statementHandler 实例的 delegate 属性对象中的 boundSql 属性对象中的 sql 属性的值
        String originalSql = (String) metaObject.getValue("delegate.boundSql.sql");
        log.info("originalSql: {}", originalSql);
        log.info("------------------StatementInterceptor#intercept 结束------------------");
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }
}
