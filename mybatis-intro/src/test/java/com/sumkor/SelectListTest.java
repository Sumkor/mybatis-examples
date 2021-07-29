package com.sumkor;

import com.sumkor.entity.Student;
import com.sumkor.mapper.StudentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * MyBatis 入门
 * https://mybatis.org/mybatis-3/zh/getting-started.html
 *
 * @author Sumkor
 * @since 2021/6/21
 */
public class SelectListTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            /**
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(java.io.Reader, java.lang.String, java.util.Properties)
             *
             * 1. 首先进行解析 mybatis-config.xml，用于构造 {@link org.apache.ibatis.session.Configuration} 对象
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parse()
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)
             *
             * 2. 利用 Configuration 对象来构造 SqlSessionFactory
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(org.apache.ibatis.session.Configuration)
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#DefaultSqlSessionFactory(org.apache.ibatis.session.Configuration)
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sqlSession.selectList
     * 关注 Executor -> StatementHandler 的流程
     */
    @Test
    public void selectAll() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            /**
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSession()
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 1. 由于 mybatis-config.xml 中的配置 transactionManager type="JDBC"，这里使用 {@link org.apache.ibatis.transaction.jdbc.JdbcTransaction} 来管理事务
             * @see org.apache.ibatis.transaction.TransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 2. 默认使用 {@link org.apache.ibatis.executor.SimpleExecutor} 作为执行器，进一步封装为缓存执行器 {@link org.apache.ibatis.executor.CachingExecutor}
             * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
             *
             * 3. 最后构造 DefaultSqlSession 对象，默认 autoCommit 为 false
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#DefaultSqlSession(org.apache.ibatis.session.Configuration, org.apache.ibatis.executor.Executor, boolean)
             */

//            List<Student> students = sqlSession.selectList("selectByPrimaryKey", 1);
            List<Student> students = sqlSession.selectList("selectAll");
            /**
             * 全限定名（比如 “com.sumkor.mapper.StudentMapper.selectAll）将被直接用于查找及使用。
             * 短名称（比如 “selectAll”）如果全局唯一也可以作为一个单独的引用。 如果不唯一，有两个或两个以上的相同名称（比如 “com.foo.selectAll” 和 “com.bar.selectAll”），那么使用时就会产生“短名称不唯一”的错误，这种情况下就必须使用全限定名。
             *
             * SqlSession 提供了在数据库执行 SQL 命令所需的所有方法。可以通过 SqlSession 实例来直接执行已映射的 SQL 语句。
             * 每个线程都应该有它自己的 SqlSession 实例。SqlSession 的实例不是线程安全的，因此是不能被共享的，所以它的最佳的作用域是请求或方法作用域。
             * 在 Web 应用中，每次收到 HTTP 请求，就可以打开一个 SqlSession，返回一个响应后，就关闭它。
             *
             * 关键位置！！！
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(String, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 1. 从 Configuration 对象中获取 sql
             *
             * 2. 执行 Executor#query
             *
             * 这里 Executor 是执行器，支持插件扩展
             * 默认使用一级缓存执行器
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.BaseExecutor#queryFromDatabase(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             *
             * ！！！这里可以看到整个流程是由 StatementHandler 串起来的
             * StatementHandler#prepare
             * StatementHandler#parameterize -> ParameterHandler#setParameters
             * StatementHandler#query -> ResultSetHandler#handleResultSets
             *
             *
             * 2.1 ParameterHandler，参数处理器，支持插件扩展
             *
             * 入口
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * 通过数据库连接对象 Connection 获取 Statement
             * @see org.apache.ibatis.executor.SimpleExecutor#prepareStatement(org.apache.ibatis.executor.statement.StatementHandler, org.apache.ibatis.logging.Log)
             * 使用参数处理器，设置参数
             * @see org.apache.ibatis.executor.statement.RoutingStatementHandler#parameterize(java.sql.Statement)
             * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#parameterize(java.sql.Statement)
             * @see org.apache.ibatis.scripting.defaults.DefaultParameterHandler#setParameters(java.sql.PreparedStatement)
             *
             * 2.2 StatementHandler，SQL语法构建器，支持插件扩展
             *
             * 入口
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.statement.RoutingStatementHandler#query(java.sql.Statement, org.apache.ibatis.session.ResultHandler)
             * 使用 Statement 执行 SQL
             * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#query(java.sql.Statement, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.logging.jdbc.PreparedStatementLogger#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
             * 反射调用
             * @see java.sql.PreparedStatement#execute()
             * 由于使用了 mysql 驱动，实际是执行，向 mysql 服务器发送数据
             * @see com.mysql.cj.jdbc.ClientPreparedStatement#execute()
             *
             * 2.3 ResultSetHandler，结果集处理器，支持插件扩展
             *
             * 入口（Statement 执行完 SQL 之后）
             * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#query(java.sql.Statement, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSets(java.sql.Statement)
             */
            for (int i = 0; i < students.size(); i++) {
                System.out.println(students.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
