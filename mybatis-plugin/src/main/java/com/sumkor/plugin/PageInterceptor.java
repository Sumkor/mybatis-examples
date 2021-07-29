package com.sumkor.plugin;

import com.sumkor.plugin.page.BoundSqlSqlSource;
import com.sumkor.plugin.page.Page;
import com.sumkor.plugin.page.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;
import java.util.StringJoiner;

/**
 * @author Sumkor
 * @since 2021/7/26
 */
@Intercepts({
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
})
@Slf4j
public class PageInterceptor implements Interceptor {

    private static final int MAPPED_STATEMENT_INDEX = 0;

    private static final int PARAMETER_INDEX = 1;

    private static final int ROW_BOUNDS_INDEX = 2;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        log.info("------------------PageInterceptor#intercept 开始------------------");
        final Object[] queryArgs = invocation.getArgs();
        final MappedStatement ms = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
        final Object parameter = queryArgs[PARAMETER_INDEX];

        // 获取分页参数
        Page pagingParam = PageUtil.getPagingParam();
        try {
            if (pagingParam != null) {
                // 构造新的 BoundSql，该对象中包含 SQL 字符串
                final BoundSql boundSql = ms.getBoundSql(parameter);
                String pagingSql = getPagingSql(boundSql.getSql(), pagingParam.getOffset(), pagingParam.getLimit());
                BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), pagingSql, boundSql.getParameterMappings(), boundSql.getParameterObject());

                // 构造新的 MappedStatement，该对象表示 XML 中的 SQL 信息
                MappedStatement mappedStatement = newMappedStatement(ms, newBoundSql);

                // 重置 MappedStatement、RowBound
                queryArgs[MAPPED_STATEMENT_INDEX] = mappedStatement;
                queryArgs[ROW_BOUNDS_INDEX] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
            }
            return invocation.proceed();
        } finally {
            log.info("------------------PageInterceptor#intercept 结束------------------");
            PageUtil.removePagingParam();
        }
    }

    /**
     * 使得当前插件生效
     *
     * 基本上，Executor、StatementHandler、ParameterHandler、ResultSetHandler 对象在创建时，都会调用 InterceptorChain#pluginAll 方法
     * @see org.apache.ibatis.session.Configuration
     *
     * 例如，创建 Executor 的调用链如下：
     * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSession()
     * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
     * @see org.apache.ibatis.plugin.InterceptorChain#pluginAll(java.lang.Object)
     * @see org.apache.ibatis.plugin.Interceptor#plugin(java.lang.Object)
     */
    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * 使用新的 BoundSql 对象，旧的 MappedStatement 对象，来构造新的 MappedStatement 对象
     */
    private MappedStatement newMappedStatement(MappedStatement ms, BoundSql newBoundSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(),
                new BoundSqlSqlSource(newBoundSql), ms.getSqlCommandType());
        builder.keyColumn(delimitedArrayToString(ms.getKeyColumns()));
        builder.keyGenerator(ms.getKeyGenerator());
        builder.keyProperty(delimitedArrayToString(ms.getKeyProperties()));
        builder.lang(ms.getLang());
        builder.resource(ms.getResource());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultOrdered(ms.isResultOrdered());
        builder.resultSets(delimitedArrayToString(ms.getResultSets()));
        builder.resultSetType(ms.getResultSetType());
        builder.timeout(ms.getTimeout());
        builder.statementType(ms.getStatementType());
        builder.useCache(ms.isUseCache());
        builder.cache(ms.getCache());
        builder.databaseId(ms.getDatabaseId());
        builder.fetchSize(ms.getFetchSize());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        return builder.build();
    }

    /**
     * 构造新的 sql： select xxx from xxx where yyy limit offset,limit
     */
    public String getPagingSql(String sql, int offset, int limit) {
        StringBuilder result = new StringBuilder(sql.length() + 100);
        result.append(sql).append(" limit ");

        if (offset > 0) {
            result.append(offset).append(",").append(limit);
        }else{
            result.append(limit);
        }
        return result.toString();
    }

    public String delimitedArrayToString(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        StringJoiner stringJoiner = new StringJoiner(",");
        for (String str : array) {
            stringJoiner.add(str);
        }
        return stringJoiner.toString();
    }
}
