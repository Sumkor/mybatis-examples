package com.sumkor;

import com.sumkor.entity.Student;
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
 * mybatis 数据库连接池
 * @see org.apache.ibatis.datasource.pooled.PooledDataSourceFactory
 * @see org.apache.ibatis.datasource.pooled.PooledDataSource
 *
 * @author Sumkor
 * @since 2021/8/3
 */
public class DataSourceTest {

    private static SqlSessionFactory sqlSessionFactory;

    /**
     * PooledDataSource 的实例化
     */
    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            /**
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(java.io.Reader, java.lang.String, java.util.Properties)
             *
             * 解析 mybatis-config.xml 文件
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parse()
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)
             *
             * 其中的 environments 节点会触发解析数据源配置，实例化事务工厂、数据库连接池工厂，再注册到 Configuration 对象中
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#environmentsElement(org.apache.ibatis.parsing.XNode)
             *
             * 由于配置了 <dataSource type="POOLED">，因此这里创建数据库连接池工厂，会触发创建数据源对象 PooledDataSource
             * @see org.apache.ibatis.datasource.pooled.PooledDataSourceFactory#PooledDataSourceFactory()
             * @see org.apache.ibatis.datasource.pooled.PooledDataSource#PooledDataSource()
             * @see org.apache.ibatis.datasource.unpooled.UnpooledDataSource#UnpooledDataSource()
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * PooledDataSource 的使用
     */
    @Test
    public void dataSource() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            /**
             * 开启会话，其中会创建事务对象、Executor 对象
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSession()
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 从事务工厂中，实例化事务对象 JdbcTransaction
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#JdbcTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             */

            Student student = sqlSession.selectOne("selectByPrimaryKey", 1);
            /**
             * 执行 SQL，这里只关注 SimpleExecutor#doQuery
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(java.lang.String, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             *
             * 构造 Statement 对象
             * @see org.apache.ibatis.executor.SimpleExecutor#prepareStatement(org.apache.ibatis.executor.statement.StatementHandler, org.apache.ibatis.logging.Log)
             * @see org.apache.ibatis.executor.BaseExecutor#getConnection(org.apache.ibatis.logging.Log)
             *
             * 其中会从事务对象中，获取数据库连接对象
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#getConnection()
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#openConnection()
             *
             * 实际是从数据源对象中，获取数据库连接对象！
             * @see org.apache.ibatis.datasource.pooled.PooledDataSource#getConnection()
             * @see org.apache.ibatis.datasource.pooled.PooledDataSource#popConnection(java.lang.String, java.lang.String)
             *
             * 1.  先看是否有空闲(idle)状态下的PooledConnection对象，如果有，就直接返回一个可用的PooledConnection对象；否则进行第2步。
             * 2.  查看活动状态的PooledConnection池activeConnections是否已满；如果没有满，则创建一个新的PooledConnection对象，然后放到activeConnections池中，然后返回此PooledConnection对象；否则进行第三步；
             * 3.  看最先进入activeConnections池中的PooledConnection对象是否已经过期：如果已经过期，从activeConnections池中移除此对象，然后创建一个新的PooledConnection对象，添加到activeConnections中，然后将此对象返回；否则进行第4步。
             * 4.  线程等待，循环2步
             */

            System.out.println("student = " + student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接的几种方式
     * @see javax.sql.DataSource#getConnection()
     *
     * 1. mysql 驱动原生方式，创建 ConnectionImpl 对象，建立 Socket 连接
     * @see com.mysql.cj.jdbc.MysqlDataSource#getConnection()
     * @see com.mysql.cj.jdbc.NonRegisteringDriver#connect(java.lang.String, java.util.Properties)
     * @see com.mysql.cj.jdbc.ConnectionImpl#ConnectionImpl(com.mysql.cj.conf.HostInfo)
     * @see com.mysql.cj.jdbc.ConnectionImpl#createNewIO(boolean)
     *
     * 2. 非池方式，跟 mysql 驱动原生方式差不多
     * @see org.apache.ibatis.datasource.unpooled.UnpooledDataSource#getConnection()
     * @see org.apache.ibatis.datasource.unpooled.UnpooledDataSource#doGetConnection(java.util.Properties)
     *
     * UNPOOLED – 这个数据源的实现会每次请求时打开和关闭连接。
     * 虽然有点慢，但对那些数据库连接可用性要求不高的简单应用程序来说，是一个很好的选择。
     * 性能表现则依赖于使用的数据库，对某些数据库来说，使用连接池并不重要，这个配置就很适合这种情形。
     *
     * 3. 池化方式
     * @see org.apache.ibatis.datasource.pooled.PooledDataSource#getConnection()
     * @see org.apache.ibatis.datasource.pooled.PooledDataSource#popConnection(java.lang.String, java.lang.String)
     *
     * POOLED– 这种数据源的实现利用“池”的概念将 JDBC 连接对象组织起来，避免了创建新的连接实例时所必需的初始化和认证时间。
     * 这种处理方式很流行，能使并发 Web 应用快速响应请求。
     */
}
