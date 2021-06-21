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
     * 使用 XML 完成 SQL 映射
     */
    @Test
    public void selectAll() {
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession();
            /**
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSession()
             * @see org.apache.ibatis.session.defaults.DefaultSqlSessionFactory#openSessionFromDataSource(org.apache.ibatis.session.ExecutorType, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 1. 默认使用 {@link org.apache.ibatis.transaction.jdbc.JdbcTransaction} 来管理事务
             * @see org.apache.ibatis.transaction.TransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             * @see org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory#newTransaction(javax.sql.DataSource, org.apache.ibatis.session.TransactionIsolationLevel, boolean)
             *
             * 2. 默认使用 {@link org.apache.ibatis.executor.SimpleExecutor} 作为执行器，默认使用缓存 {@link org.apache.ibatis.executor.CachingExecutor}
             * @see org.apache.ibatis.session.Configuration#newExecutor(org.apache.ibatis.transaction.Transaction, org.apache.ibatis.session.ExecutorType)
             *
             * 3. 最后构造 DefaultSqlSession 对象
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#DefaultSqlSession(org.apache.ibatis.session.Configuration, org.apache.ibatis.executor.Executor, boolean)
             */

            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
            /**
             * 获取 Mapper 接口的动态代理实例
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#getMapper(java.lang.Class)
             * @see org.apache.ibatis.session.Configuration#getMapper(java.lang.Class, org.apache.ibatis.session.SqlSession)
             * @see org.apache.ibatis.binding.MapperRegistry#getMapper(java.lang.Class, org.apache.ibatis.session.SqlSession)
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(org.apache.ibatis.session.SqlSession)
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(org.apache.ibatis.binding.MapperProxy)
             */
            List<Student> students = studentMapper.selectAll();
            for (int i = 0; i < students.size(); i++) {
                System.out.println(students.get(i));
            }

//            students = sqlSession.selectList("selectAll");
//            System.out.println("students = " + students);
            Thread.sleep(10000000);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }

    /**
     * 使用注解完成 SQL 映射
     */
    @Test
    public void selectByPrimaryKey() {
        SqlSession sqlSession = null;
        try {
            sqlSession = sqlSessionFactory.openSession();
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
            Student student = studentMapper.selectByPrimaryKey(1);
            System.out.println(student);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
            }
        }
    }
}
