package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sumkor
 * @since 2021/8/18
 */
public class SelectByParamTest {

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
     * 字符串替换
     * 默认情况下，使用 #{} 参数语法时，MyBatis 会创建 PreparedStatement 参数占位符，并通过占位符安全地设置参数（就像使用 ? 一样）。
     * 这样做更安全，更迅速，通常也是首选做法，不过有时你就是想直接在 SQL 语句中直接插入一个不转义的字符串。 比如 ORDER BY 子句，这时候你可以：
     * ORDER BY ${columnName}
     *
     * https://mybatis.org/mybatis-3/zh/sqlmap-xml.html
     */
    @Test
    public void selectByParam() throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Date beginDate = DateUtils.parseDate("2018-08-29 00:00:00", "yyyy-MM-dd HH:mm:ss");
            Date endDate = DateUtils.parseDate("2018-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            Map<Object, Object> map = new HashMap<>();
            map.put("bTime", beginDate);
            map.put("eTime", endDate);
            map.put("columnName", "gmt_created");
            List<Student> students = sqlSession.selectList("selectBetweenCreatedTime", map);
            /**
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(java.lang.String, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 这里的 BoundSql boundSql = ms.getBoundSql(parameterObject);
             * 得到的 BoundSql 实例，得到了预编译后的 SQL 语句
             *
             * 关注从 MappedStatement 对象中，组装动态 SQL 的过程：
             * @see org.apache.ibatis.mapping.MappedStatement#getBoundSql(java.lang.Object)
             * @see org.apache.ibatis.scripting.xmltags.DynamicSqlSource#getBoundSql(java.lang.Object)
             * @see org.apache.ibatis.scripting.xmltags.MixedSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
             *
             * 解析 ${} 中的变量
             * @see org.apache.ibatis.scripting.xmltags.TextSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
             */
            System.out.println("students = " + students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
