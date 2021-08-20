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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sumkor
 * @since 2021/8/20
 */
public class InsertTest {

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
     * useGeneratedKeys（仅适用于 insert 和 update）
     * 这会令 MyBatis 使用 JDBC 的 getGeneratedKeys 方法来取出由数据库内部生成的主键（比如：像 MySQL 和 SQL Server 这样的关系型数据库管理系统的自动递增字段），
     * 默认值：false。
     */
    @Test
    public void insert() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        Student student = new Student();
        student.setName("插入01");
        student.setPhone("123456");
        student.setEmail("test@test.com");
        student.setSex((byte) 1);
        int result = sqlSession.insert("insert", student);
        /**
         * @see org.apache.ibatis.session.defaults.DefaultSqlSession#insert(java.lang.String, java.lang.Object)
         * @see org.apache.ibatis.executor.SimpleExecutor#doUpdate(org.apache.ibatis.mapping.MappedStatement, java.lang.Object)
         *
         * 创建 PreparedStatement
         * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#instantiateStatement(java.sql.Connection)
         *
         * 执行 SQL
         * @see org.apache.ibatis.executor.statement.PreparedStatementHandler#update(java.sql.Statement)
         */
        System.out.println("result = " + result);

        Integer id = student.getId();
        System.out.println("id = " + id);
    }
    /**
     * 执行结果：
     *
     * 2021-08-20 10:32:40,325 [main] DEBUG [org.apache.ibatis.datasource.pooled.PooledDataSource] - Created connection 1332668132.
     * 2021-08-20 10:32:40,327 [main] DEBUG [com.sumkor.mapper.StudentMapper.insert] - ==>  Preparing: insert into student (name, phone, email, sex, locked) values (?, ?, ?, ?, ?)
     * 2021-08-20 10:32:40,365 [main] DEBUG [com.sumkor.mapper.StudentMapper.insert] - ==> Parameters: 插入01(String), 123456(String), test@test.com(String), 1(Byte), null
     * 2021-08-20 10:32:40,391 [main] DEBUG [com.sumkor.mapper.StudentMapper.insert] - <==    Updates: 1
     * result = 1
     * id = 11
     */

    @Test
    public void insertBatch() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        List<Student> students = new ArrayList<>();

        Student student01 = new Student();
        student01.setName("测试01");
        student01.setPhone("123456");
        student01.setEmail("test@test.com");
        student01.setSex((byte) 1);
        students.add(student01);

        Student student02 = new Student();
        student02.setName("测试02");
        student02.setPhone("123456");
        student02.setEmail("test@test.com");
        student02.setSex((byte) 1);
        students.add(student02);

        int result = sqlSession.insert("insertBatch", students);
        System.out.println("result = " + result);
        System.out.println("students = " + students);
    }
    /**
     * 2021-08-20 14:41:14,962 [main] DEBUG [com.sumkor.mapper.StudentMapper.insertBatch] - ==>  Preparing: insert into student (name, phone, email, sex, locked) values (?, ?, ?, ?, ?) , (?, ?, ?, ?, ?)
     * 2021-08-20 14:41:15,029 [main] DEBUG [com.sumkor.mapper.StudentMapper.insertBatch] - ==> Parameters: 测试01(String), 123456(String), test@test.com(String), 1(Byte), null, 测试02(String), 123456(String), test@test.com(String), 1(Byte), null
     * 2021-08-20 14:41:15,041 [main] DEBUG [com.sumkor.mapper.StudentMapper.insertBatch] - <==    Updates: 2
     * result = 2
     * students = [Student{id=13, name='测试01'}, Student{id=14, name='测试02'}]
     */
}
