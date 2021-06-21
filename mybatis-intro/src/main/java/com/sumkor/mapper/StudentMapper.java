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

    List<Student> selectAll();

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student selectByPrimaryKey(int id);

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
}