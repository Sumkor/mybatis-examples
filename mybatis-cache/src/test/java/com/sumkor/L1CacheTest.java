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

/**
 * 一级缓存的生命周期和 SqlSession 一致。
 * 一级缓存内部设计简单，PerpetualCache 只是一个没有容量限定的 HashMap，在缓存的功能性上有所欠缺。
 * 一级缓存最大范围是 SqlSession 内部，有多个 SqlSession 或者分布式的环境下，数据库写操作会引起脏数据，建议设定缓存级别为 Statement。
 *
 * @author Sumkor
 * @since 2021/8/6
 */
public class L1CacheTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 同一个 SqlSession 下，查询同一条 SQL，会命中缓存
     *
     * selectByPrimaryKey 方法对应的 SQL 不是写在 xml 文件中的，因此二级缓存对其不生效
     * @see StudentMapper#selectByPrimaryKey(int)
     */
    @Test
    public void cache() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            /**
             * 开启会话，其中会创建 Executor 对象
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSession()
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
             */

            // 第一次查询，加入缓存
            Student student01 = sqlSession.selectOne("selectByPrimaryKey", 1);
            /**
             * 通过 Executor 对象来执行 SQL
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(String, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 入口 Executor#query
             * @see org.apache.ibatis.executor.Executor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 1. 生成 cacheKey
             * @see org.apache.ibatis.executor.BaseExecutor#createCacheKey(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.mapping.BoundSql)
             *
             * 格式 = HashCode + Statement Id + Offset + Limit + Sql + Params
             * cacheKey = "-2126011546:2099012989:com.sumkor.mapper.StudentMapper.selectByPrimaryKey:0:2147483647:SELECT * FROM student WHERE id = ?:1:development"
             *
             * 2. 查询缓存（二级缓存）
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             *
             * 3. 查询缓存（一级缓存）
             * @see org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             *
             * 向数据库发起查询，并将查询结果写入一级缓存
             * @see org.apache.ibatis.executor.BaseExecutor#queryFromDatabase(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             *
             * 4. 查询数据库
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             */
            System.out.println("\r\n student01 = " + student01 + "\r\n");

            // 第二次查询（相同SQL），直接从缓存获取
            Student student02 = sqlSession.selectOne("selectByPrimaryKey", 1);
            System.out.println("\r\n student02 = " + student02 + "\r\n");

            // 第二次查询（不同SQL），从数据库查询
            Student student03 = sqlSession.selectOne("selectByPrimaryKey", 2);
            System.out.println("\r\n student03 = " + student03 + "\r\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
        /**
         * 执行结果：
         *
         * 2021-08-09 17:02:32,611 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Opening JDBC Connection
         * 2021-08-09 17:02:32,962 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Created connection 369049246.
         * 2021-08-09 17:02:32,962 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Setting autocommit to false on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@15ff3e9e]
         * 2021-08-09 17:02:32,968 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==>  Preparing: SELECT * FROM student WHERE id = ?
         * 2021-08-09 17:02:33,028 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==> Parameters: 1(Integer)
         * 2021-08-09 17:02:33,054 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==    Columns: id, name, phone, email, sex, locked, gmt_created, gmt_modified, delete
         * 2021-08-09 17:02:33,056 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==        Row: 1, 小明, 13821378270, xiaoming@mybatis.cn, 1, 0, 2018-08-29 18:27:42, 2018-10-08 20:54:25, null
         * 2021-08-09 17:02:33,058 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==      Total: 1
         *
         * student01 = Student{id=1, name='小明', phone='13821378270', email='xiaoming@mybatis.cn', sex=1, locked=0, gmtCreated=null, gmtModified=null}
         *
         * student02 = Student{id=1, name='小明', phone='13821378270', email='xiaoming@mybatis.cn', sex=1, locked=0, gmtCreated=null, gmtModified=null}
         *
         * 2021-08-09 17:02:33,059 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==>  Preparing: SELECT * FROM student WHERE id = ?
         * 2021-08-09 17:02:33,059 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - ==> Parameters: 2(Integer)
         * 2021-08-09 17:02:33,060 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==    Columns: id, name, phone, email, sex, locked, gmt_created, gmt_modified, delete
         * 2021-08-09 17:02:33,061 [main] TRACE [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==        Row: 2, 大明, 13821378271, xiaoli@mybatis.cn, 0, 0, 2018-08-30 18:27:42, 2018-10-08 20:54:29, null
         * 2021-08-09 17:02:33,071 [main] DEBUG [com.sumkor.mapper.StudentMapper.selectByPrimaryKey] - <==      Total: 1
         *
         * student03 = Student{id=2, name='大明', phone='13821378271', email='xiaoli@mybatis.cn', sex=0, locked=0, gmtCreated=null, gmtModified=null}
         *
         * 2021-08-09 17:02:33,071 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Resetting autocommit to true on JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@15ff3e9e]
         * 2021-08-09 17:02:33,072 [main] DEBUG [org.apache.ibatis.transaction.jdbc.JdbcTransaction] - Closing JDBC Connection [com.mysql.cj.jdbc.ConnectionImpl@15ff3e9e]
         * 2021-08-09 17:02:33,073 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Returned connection 369049246 to pool.
         */
    }

    /**
     * 刷新缓存是清空这个 SqlSession 的所有缓存，不单单是某个键。
     * 本地缓存和二级缓存都会被清空！
     */
    @Test
    public void flushCache() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

            // 第一次查询（相同SQL），加入缓存
            Student student01 = sqlSession.selectOne("selectByPrimaryKey", 1);
            System.out.println("\r\n student01 = " + student01 + "\r\n");

            // 刷新缓存（不同SQL）
            Student student02 = sqlSession.selectOne("selectByName", "小明");
            System.out.println("\r\n student02 = " + student02 + "\r\n");
            /**
             * @see org.apache.ibatis.executor.BaseExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
             */

            // 第二次查询（相同SQL），无缓存
            Student student03 = sqlSession.selectOne("selectByPrimaryKey", 1);
            System.out.println("\r\n student03 = " + student03 + "\r\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
