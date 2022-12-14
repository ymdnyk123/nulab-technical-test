package com.example.demo.mybatis.mapper;

import com.example.demo.mybatis.model.WorkHours;
import com.example.demo.mybatis.model.WorkHoursExample;
import com.example.demo.mybatis.model.WorkHoursKey;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkHoursMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    long countByExample(WorkHoursExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int deleteByExample(WorkHoursExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int deleteByPrimaryKey(WorkHoursKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int insert(WorkHours row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int insertSelective(WorkHours row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    List<WorkHours> selectByExample(WorkHoursExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    WorkHours selectByPrimaryKey(WorkHoursKey key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int updateByExampleSelective(@Param("row") WorkHours row, @Param("example") WorkHoursExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int updateByExample(@Param("row") WorkHours row, @Param("example") WorkHoursExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int updateByPrimaryKeySelective(WorkHours row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table work_hours
     *
     * @mbg.generated Sat Jul 02 01:02:51 JST 2022
     */
    int updateByPrimaryKey(WorkHours row);
}