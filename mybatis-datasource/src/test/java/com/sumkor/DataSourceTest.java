package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;

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
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(Reader, String, java.util.Properties)
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
     * PooledDataSource 的使用：从数据库连接池中取出连接，收回连接
     */
    @Test
    public void dataSource() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
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

        Student student01 = sqlSession.selectOne("selectByPrimaryKey", 1);
        /**
         * 执行 SQL，这里只关注 SimpleExecutor#doQuery
         * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(String, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
         * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
         *
         * 构造 Statement 对象
         * @see org.apache.ibatis.executor.SimpleExecutor#prepareStatement(org.apache.ibatis.executor.statement.StatementHandler, org.apache.ibatis.logging.Log)
         *
         * 其中会先获取数据库连接对象，再利用 Connection 来创建 Statement
         * @see org.apache.ibatis.executor.BaseExecutor#getConnection(org.apache.ibatis.logging.Log)
         *
         * 由事务对象 JdbcTransaction 来控制对数据库连接对象的获取
         * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#getConnection()
         * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#openConnection()
         *
         * 实际是从数据源对象（这里是数据库连接池 PooledDataSource）中，获取数据库连接对象！
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#getConnection()
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#popConnection(String, String)
         */
        System.out.println("student01 = " + student01);

        Student student02 = sqlSession.selectOne("selectByPrimaryKey", 2);
        /**
         * 这里拿到的连接对象，跟上一次查询的是同一个吗？是的！PooledConnection 实例是一样的，原始的 ConnectionImpl 也是一样的。
         * @see org.apache.ibatis.executor.BaseExecutor#getConnection(org.apache.ibatis.logging.Log)
         *
         * 因为 JdbcTransaction 在一次会话中是单例的，因此在同一次会话使用同一个数据库连接
         * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#getConnection()
         *
         * 这里拿到的 Statement 对象，跟上一次查询的是同一个吗？不是！
         * 因为 StatementHandler#prepare 每次查询都会构建新的 Statement 对象。
         * @see org.apache.ibatis.executor.SimpleExecutor#prepareStatement(org.apache.ibatis.executor.statement.StatementHandler, org.apache.ibatis.logging.Log)
         * @see org.apache.ibatis.executor.statement.BaseStatementHandler#prepare(java.sql.Connection, java.lang.Integer)
         * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#instantiateStatement(java.sql.Connection)
         */
        System.out.println("student02 = " + student02);

        sqlSession.close();
        /**
         * 关闭数据库会话
         * @see org.apache.ibatis.session.defaults.DefaultSqlSession#close()
         * @see org.apache.ibatis.executor.BaseExecutor#close(boolean)
         * @see org.apache.ibatis.transaction.jdbc.JdbcTransaction#close()
         *
         * 实际是将数据库连接，放回连接池中
         * @see org.apache.ibatis.datasource.pooled.PooledConnection#invoke(Object, java.lang.reflect.Method, Object[])
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#pushConnection(org.apache.ibatis.datasource.pooled.PooledConnection)
         */
    }

    /**
     * Connection 在数据库连接池中，如何维护有效性？
     */
    @Test
    public void valid() throws SQLException {
        // 建立会话
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Connection connection = sqlSession.getConnection();
        /**
         * 这里拿到的是由 PooledConnection 代理的连接对象，其代理方法为
         * @see org.apache.ibatis.datasource.pooled.PooledConnection#invoke(Object, java.lang.reflect.Method, Object[])
         */
        boolean valid = connection.isValid(1000);
        System.out.println("valid = " + valid);

        // 关闭会话
        sqlSession.close();
        /**
         * SqlSession 关闭之后：
         *
         * 一方面，用户已经把 Connection 归还给连接池了，所以用户不能继续操作这个 Connection，需要表现为失效了。
         * 一方面，连接池的存在就是为了让 Connection 能够复用，不能只用一次就丢掉，需要保持有效性。
         *
         * Mybatis 对此的做法是，为 Connection 对象生成动态代理。
         * 用户用的是代理对象，而代理对象是短命的，用完就丢掉。
         *
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#pushConnection(org.apache.ibatis.datasource.pooled.PooledConnection)
         */

        // assert 无效的连接
        connection.isValid(1000);
        /**
         * @see org.apache.ibatis.datasource.pooled.PooledConnection#checkConnection()
         */
    }

    /**
     * 验证检出超时
     */
    @Test
    public void timeout() throws InterruptedException {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        Environment environment = configuration.getEnvironment();
        PooledDataSource pooledDataSource = (PooledDataSource) environment.getDataSource();
        System.out.println("pooledDataSource = " + pooledDataSource);

        pooledDataSource.setPoolMaximumActiveConnections(1); // 活跃连接集合的容量为1
        pooledDataSource.setPoolMaximumCheckoutTime(1000);   // 最大检出时间为1秒

        /**
         * 关键代码
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#popConnection(String, String)
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#pushConnection(org.apache.ibatis.datasource.pooled.PooledConnection)
         */

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        // 线程一，检出，很久不归还
        Thread thread01 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " start to open session...");
                try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                    Student student = sqlSession.selectOne("selectByPrimaryKey", 1);
                    System.out.println("student = " + student);

                    // 休眠5秒之后，才让线程二获取连接
                    Thread.sleep(5000);
                    startLatch.countDown();

                    // 继续休眠1秒，再获取连接，发现被线程二设为已失效
                    Thread.sleep(1000);
                    sqlSession.getConnection().isValid(1000);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }
        }, "thread_01");

        // 线程二，在线程一检出过一段时间之后，再检出
        Thread thread02 = new Thread(new Runnable() {
            @Override
            public void run() {
                SqlSession sqlSession = null;
                try {
                    startLatch.await();
                    System.out.println(Thread.currentThread().getName() + " start to open session...");
                    sqlSession = sqlSessionFactory.openSession();
                    Student student = sqlSession.selectOne("selectByPrimaryKey", 2);
                    System.out.println("student = " + student);
                    // 此时空闲连接集合为空，且活跃连接集合已满，则需要判读活跃连接集合中的连接，是否检出超时：
                    // 1. 超时，作废该连接；
                    // 2. 未超时，等待释放
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (sqlSession != null) {
                        sqlSession.close();
                    }
                    endLatch.countDown();
                }
            }
        }, "thread_02");

        thread01.start();
        thread02.start();

        endLatch.await();
    }

    /**
     * 如果使用时，检测到连接无效，会怎么处理
     *
     * 配置
     * <property name="poolPingEnabled" value="true"/>
     * <property name="poolPingQuery" value="select 1 from abc"/>
     */
    @Test
    public void ping() {
        /**
         * 关键代码
         * @see org.apache.ibatis.datasource.pooled.PooledDataSource#pingConnection
         *
         * 在执行 ping 之前，会检查 距离上一次使用连接的时间，大于连接检查频率
         *
         * 而这里第一次从连接池中拿到连接，ping失败了，当作坏连接而作废掉，重新建立新连接，再校验新连接是否有效。
         * 由于建立新连接与校验新连接是否有效，是发生在同一毫秒内，因此不会进入 ping 逻辑，因此检出成功。
         * @see org.apache.ibatis.datasource.pooled.PooledConnection#getTimeElapsedSinceLastUse()
         */
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Student student = sqlSession.selectOne("selectByPrimaryKey", 2);
        System.out.println("student = " + student);
        sqlSession.close();
    }
    /**
     * 执行结果：
     *
     * 情况一：
     *
     * 2021-08-24 23:47:24,858 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - PooledDataSource: Could not get a good connection to the database.
     * org.apache.ibatis.exceptions.PersistenceException:
     * ### Error querying database.  Cause: java.sql.SQLException: PooledDataSource: Could not get a good connection to the database.
     * ### The error may exist in com/sumkor/mapper/StudentMapper.java (best guess)
     * ### The error may involve com.sumkor.mapper.StudentMapper.selectByPrimaryKey
     * ### The error occurred while executing a query
     * ### Cause: java.sql.SQLException: PooledDataSource: Could not get a good connection to the database.
     *
     * 情况二：
     *
     * 2021-08-25 00:13:17,830 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Opening JDBC Connection
     * Loading class `com.mysql.jdbc.Driver'. This is deprecated. The new driver class is `com.mysql.cj.jdbc.Driver'. The driver is automatically registered via the SPI and manual loading of the driver class is generally unnecessary.
     * 2021-08-25 00:13:18,487 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Created connection 112797691.
     * 2021-08-25 00:13:18,487 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Testing connection 112797691 ...
     * 2021-08-25 00:13:18,516 [main] WARN  [org.apache.ibatis.datasource.pooled.PooledDataSource] - Execution of ping query 'select 1 from abc' failed: Table 'testdb.abc' doesn't exist
     * 2021-08-25 00:13:18,523 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Connection 112797691 is BAD: Table 'testdb.abc' doesn't exist
     * 2021-08-25 00:13:18,523 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - A bad connection (112797691) was returned from the pool, getting another connection.
     * 2021-08-25 00:13:18,550 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Created connection 112049309.
     * 2021-08-25 00:13:18,550 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@6adbc9d]
     * 2021-08-25 00:13:18,561 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==>  Preparing: SELECT * FROM student WHERE id = ?
     * 2021-08-25 00:13:18,603 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==> Parameters: 2(Integer)
     * 2021-08-25 00:13:18,638 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==    Columns: id, name, phone, email, sex, locked, gmt_created, gmt_modified, delete
     * 2021-08-25 00:13:18,640 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==        Row: 2, 大明, 13821378271, xiaoli@mybatis.cn, 0, 0, 2018-08-30 18:27:42, 2018-10-08 20:54:29, null
     * 2021-08-25 00:13:18,643 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==      Total: 1
     * student = Student{id=2, name='大明'}
     * 2021-08-25 00:13:18,643 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@6adbc9d]
     * 2021-08-25 00:13:18,644 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@6adbc9d]
     * 2021-08-25 00:13:18,644 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Testing connection 112049309 ...
     * 2021-08-25 00:13:18,645 [main] WARN  [org.apache.ibatis.datasource.pooled.PooledDataSource] - Execution of ping query 'select 1 from abc' failed: Table 'testdb.abc' doesn't exist
     * 2021-08-25 00:13:18,645 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Connection 112049309 is BAD: Table 'testdb.abc' doesn't exist
     * 2021-08-25 00:13:18,645 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - A bad connection (112049309) attempted to return to the pool, discarding connection.
     *
     */

    /**
     * 获取数据库连接的几种方式
     * @see javax.sql.DataSource#getConnection()
     *
     * 1. mysql 驱动原生方式，创建 ConnectionImpl 对象，建立 Socket 连接
     * @see com.mysql.cj.jdbc.MysqlDataSource#getConnection()
     * @see com.mysql.cj.jdbc.NonRegisteringDriver#connect(String, java.util.Properties)
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
     * @see org.apache.ibatis.datasource.pooled.PooledDataSource#popConnection(String, String)
     *
     * POOLED– 这种数据源的实现利用“池”的概念将 JDBC 连接对象组织起来，避免了创建新的连接实例时所必需的初始化和认证时间。
     * 这种处理方式很流行，能使并发 Web 应用快速响应请求。
     */
}
