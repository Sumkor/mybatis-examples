package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.Reader;
import java.util.Properties;

/**
 * @author Sumkor
 * @since 2021/8/6
 */
public class MultiDataSourceTest {

    @Test
    public void multi() throws Exception {
        // 数据源-默认
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory01 = new SqlSessionFactoryBuilder().build(reader);
        SqlSession sqlSession01 = sqlSessionFactory01.openSession();
        Student student01 = sqlSession01.selectOne("selectByPrimaryKey", 1);
        System.out.println("student01 = " + student01);
        sqlSession01.close();

        // 数据源-test
        reader = Resources.getResourceAsReader("mybatis-config.xml"); // 重新打开文件流
        Properties properties = new Properties();
        properties.setProperty("jdbc.username", "root");
        properties.setProperty("jdbc.password", "root");
        SqlSessionFactory sqlSessionFactory02 = new SqlSessionFactoryBuilder().build(reader, "test", properties);
        SqlSession sqlSession02 = sqlSessionFactory02.openSession();
        Student student02 = sqlSession02.selectOne("selectByPrimaryKey", 1);
        System.out.println("student02 = " + student02);
        sqlSession02.close();
    }
}
