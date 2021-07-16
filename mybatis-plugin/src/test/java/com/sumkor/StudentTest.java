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
import java.util.concurrent.ThreadLocalRandom;

/**
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sqlSession.getMapper
     */
    @Test
    public void selectByPrimaryKey() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
            Student student = studentMapper.selectByPrimaryKey(1);
            System.out.println(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            long l = random.nextLong(15, 20);
            System.out.println(l);
        }
    }
}
