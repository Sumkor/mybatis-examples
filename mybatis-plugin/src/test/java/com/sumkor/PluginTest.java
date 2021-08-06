package com.sumkor;

import com.sumkor.entity.Student;
import com.sumkor.plugin.PageInterceptor;
import com.sumkor.plugin.StatementInterceptor;
import com.sumkor.plugin.page.PageUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * @author Sumkor
 * @since 2021/6/21
 */
public class PluginTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            /**
             * 解析 mybatis-config.xml
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(java.io.Reader, java.lang.String, java.util.Properties)
             * 解析配置，其中会解析 plugins 标签
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)
             *
             * 实例化插件配置类，注入 {@link org.apache.ibatis.plugin.InterceptorChain} 之中，即注册到 Configuration 对象中
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#pluginElement(org.apache.ibatis.parsing.XNode)
             * @see org.apache.ibatis.session.Configuration#addInterceptor(org.apache.ibatis.plugin.Interceptor)
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 定义了两个插件
     * 其中 {@link PageInterceptor} 用于拦截 Executor#query 方法
     * 其中 {@link StatementInterceptor} 用于拦截 StatementHandler#prepare 方法
     */
    @Test
    public void page() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            /**
             * 开启会话
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 构造 Executor，使用插件进行包装（每一次 openSession 操作，都会创建一个新的 Executor 对象，涉及到的各种代理也会重新生成一遍）
             * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
             *
             * 遍历所有的插件，判断插件是否属于当前 Executor
             * @see org.apache.ibatis.plugin.InterceptorChain#pluginAll(java.lang.Object)
             *
             * 如何判断？通过调用每一个插件的 {@link Interceptor#plugin(java.lang.Object)} 方法
             * @see com.sumkor.plugin.PageInterceptor#plugin(java.lang.Object)
             * @see org.apache.ibatis.plugin.Plugin#wrap(java.lang.Object, org.apache.ibatis.plugin.Interceptor)
             *
             * 基本上，Executor、StatementHandler、ParameterHandler、ResultSetHandler 对象在创建时，都会调用 InterceptorChain#pluginAll 方法，进而触发插件的动态代理。
             * 这些对象的创建入口，都封装在 Configuration 类中。
             */

            PageUtil.setPagingParam(1, 2);
            List<Student> students = sqlSession.selectList("selectAll");
            /**
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(java.lang.String, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 由于启用了 Executor 插件，这里执行代理方法
             * @see org.apache.ibatis.executor.Executor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.plugin.Plugin#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
             *
             * 进入插件的代理方法
             * @see com.sumkor.plugin.PageInterceptor#intercept(org.apache.ibatis.plugin.Invocation)
             *
             * 进入原始 Executor 逻辑
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             *
             * 关键位置！
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             *
             * 首先，创建 StatementHandler 对象
             * @see org.apache.ibatis.session.Configuration#newStatementHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.statement.RoutingStatementHandler#RoutingStatementHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#PreparedStatementHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.statement.BaseStatementHandler#BaseStatementHandler(org.apache.ibatis.executor.Executor, org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * 其中会依次创建 ParameterHandler、ResultSetHandler、StatementHandler 对象，每次创建都会遍历插件列表，进行匹配组装
             *
             * 接着，执行各个 Handler 的方法
             * StatementHandler#prepare
             * StatementHandler#parameterize -> ParameterHandler#setParameters
             * StatementHandler#query -> ResultSetHandler#handleResultSets
             */
            for (int i = 0; i < students.size(); i++) {
                System.out.println(students.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MyBatis 如何利用 RowBounds 实现通用分页？
     * https://blog.csdn.net/u010077905/article/details/38469653
     */
    @Test
    public void rowBounds() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            /**
             * MyBatis 提供了 RowBounds 类，用于实现分页查询。 RowBounds 中有两个数字，offset 和 limit。
             * 具体实现，就是先把数据全部查询到 ResultSet，然后从 ResultSet 中取出 offset 和 limit 之间的数据，这就实现了分页查询。
             * @see org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleRowValuesForSimpleResultMap(org.apache.ibatis.executor.resultset.ResultSetWrapper, org.apache.ibatis.mapping.ResultMap, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.session.RowBounds, org.apache.ibatis.mapping.ResultMapping)
             */
            RowBounds rowBounds = new RowBounds(1, 2);
            List<Student> students = sqlSession.selectList("selectAll", null, rowBounds);
            for (int i = 0; i < students.size(); i++) {
                System.out.println(students.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每一次 openSession 操作，都会生成新的 Executor 对象。
     * 如果配置了插件，每个 Executor 对象都会生成各自的代理。
     */
    @Test
    public void openSession() {
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        SqlSession sqlSession02 = sqlSessionFactory.openSession();
        /**
         * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
         */
        System.out.println();
        sqlSession01.close();
        sqlSession02.close();
    }
}
