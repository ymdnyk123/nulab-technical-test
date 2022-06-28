package com.example.demo.mybatis.mapper;

import com.example.demo.mybatis.model.StatusT01;
import com.example.demo.mybatis.model.StatusT01Example;
import com.example.demo.mybatis.model.StatusT01Key;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatusT01Mapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    long countByExample(StatusT01Example example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int deleteByExample(StatusT01Example example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int deleteByPrimaryKey(StatusT01Key key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int insert(StatusT01 row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int insertSelective(StatusT01 row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    List<StatusT01> selectByExample(StatusT01Example example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    StatusT01 selectByPrimaryKey(StatusT01Key key);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int updateByExampleSelective(@Param("row") StatusT01 row, @Param("example") StatusT01Example example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int updateByExample(@Param("row") StatusT01 row, @Param("example") StatusT01Example example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int updateByPrimaryKeySelective(StatusT01 row);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table status_t01
     *
     * @mbg.generated Thu Jun 23 02:06:25 JST 2022
     */
    int updateByPrimaryKey(StatusT01 row);
}