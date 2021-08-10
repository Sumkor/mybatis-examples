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

/**
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
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.SimpleExecutor#doQuery(org.apache.ibatis.mapping.MappedStatement, Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.mapping.BoundSql)
             */
            System.out.println("student01 = " + student01);

            // 第二次查询（相同SQL），直接从缓存获取
            Student student02 = sqlSession.selectOne("selectByPrimaryKey", 1);
            System.out.println("student02 = " + student02);

            // 第二次查询（不同SQL），从数据库查询
            Student student03 = sqlSession.selectOne("selectByPrimaryKey", 2);
            System.out.println("student03 = " + student03);

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
}
