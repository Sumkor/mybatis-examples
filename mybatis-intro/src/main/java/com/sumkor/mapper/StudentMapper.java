package com.sumkor.mapper;

import com.sumkor.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Sumkor
 * @since 2021/6/21
 */
public interface StudentMapper {

    /**
     * 查询全部
     */
    List<Student> selectAll();

    /**
     * 根据 id 查询
     * 由注解指定 SQL，因此 xml 文件中的二级缓存 <cache/> 配置，对该 SQL 无效
     */
    @Select("SELECT * FROM student WHERE id = #{id}")
    Student selectByPrimaryKey(int id);

    /**
     * 根据名字查询，配置了 flushCache = true
     */
    Student selectByName(String name);

    /**
     * 根据 id 更新
     */
    int updateByPrimaryKey(Student student);

    /**
     * 获取一段时间内的用户
     */
    List<Student> selectBetweenCreatedTime(Map<String, Object> params);

    /**
     * @param bTime 开始时间
     * @param eTime 结束时间
     */
    List<Student> selectBetweenCreatedTimeParam(@Param("bTime") Date bTime, @Param("eTime") Date eTime);

    /**
     * 插入，id 自增
     */
    int insert(Student student);
}