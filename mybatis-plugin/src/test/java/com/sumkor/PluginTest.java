package com.sumkor;

import com.sumkor.entity.Student;
import com.sumkor.plugin.PageInterceptor;
import com.sumkor.plugin.StatementInterceptor;
import com.sumkor.plugin.page.PageUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.plugin.Interceptor;
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
             * 解析插件配置
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
             * 构造 Executor，使用插件进行包装
             * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
             *
             * 遍历所有的插件，判断插件是否属于当前 Executor
             * @see org.apache.ibatis.plugin.InterceptorChain#pluginAll(java.lang.Object)
             * 如何判断？通过调用每一个插件的 {@link Interceptor#plugin(java.lang.Object)} 方法
             * @see org.apache.ibatis.plugin.Plugin#wrap(java.lang.Object, org.apache.ibatis.plugin.Interceptor)
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
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             *
             * StatementHandler#prepare
             * StatementHandler#parameterize -> ParameterHandler#setParameters
             * StatementHandler#query -> ResultSetHandler#handleResultSets
             *
             *
             */
            for (int i = 0; i < students.size(); i++) {
                System.out.println(students.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
