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
public class StudentTest {

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
             * 2.0 Executor，执行器，支持拦截
             *
             * 默认使用一级缓存
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.BaseExecutor#queryFromDatabase(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             *
             * 2.1 ParameterHandler，参数处理器，支持拦截
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
             * 2.2 StatementHandler，SQL语法构建器，支持拦截
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
             * 2.3 ResultSetHandler，结果集处理器，支持拦截
             *
             * 入口（Statement 执行完 SQL 之后）
             * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#query(java.sql.Statement, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.resultset.DefaultResultSetHandler#handleResultSets(java.sql.Statement)
             */
            System.out.println("students = " + students);
            Thread.sleep(10000000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sqlSession.getMapper
     */
    @Test
    public void selectByPrimaryKey() {
        /**
         * 构建 SqlSessionFactory 时，会解析 mybatis-config.xml 文件，其中的 mapper 节点会触发解析 SQL XML 文件
         * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parse()
         * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)
         *
         * 解析 SQL XML 文件，将 mapper 接口类注册至 Configuration 对象中
         * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#mapperElement(org.apache.ibatis.parsing.XNode)
         * @see org.apache.ibatis.session.Configuration#addMapper(java.lang.Class)
         * @see org.apache.ibatis.binding.MapperRegistry#addMapper(java.lang.Class)
         */
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
            /**
             * 虽然从技术层面上来讲，任何映射器实例的最大作用域与请求它们的 SqlSession 相同。但方法作用域才是映射器实例的最合适的作用域。
             * 也就是说，映射器实例应该在调用它们的方法中被获取，使用完毕之后即可丢弃。 映射器实例并不需要被显式地关闭
             *
             * 获取 Mapper 接口的动态代理实例
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#getMapper(Class)
             * @see org.apache.ibatis.session.Configuration#getMapper(Class, SqlSession)
             * @see org.apache.ibatis.binding.MapperRegistry#getMapper(Class, SqlSession)
             *
             * JDK 动态代理
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(SqlSession)
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(org.apache.ibatis.binding.MapperProxy)
             */
            Student student = studentMapper.selectByPrimaryKey(1);
            System.out.println(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
