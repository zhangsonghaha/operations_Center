package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsReleaseApply;

/**
 * Release Apply Mapper Interface
 * 
 * @author ruoyi
 */
public interface OpsReleaseApplyMapper 
{
    /**
     * Query Release Apply
     * 
     * @param id Release Apply ID
     * @return Release Apply
     */
    public OpsReleaseApply selectOpsReleaseApplyById(Long id);

    /**
     * Query Release Apply List
     * 
     * @param opsReleaseApply Release Apply
     * @return Release Apply List
     */
    public List<OpsReleaseApply> selectOpsReleaseApplyList(OpsReleaseApply opsReleaseApply);

    /**
     * Query Release Apply List by Process Instance IDs
     *
     * @param processInstanceIds Process Instance IDs
     * @return Release Apply List
     */
    public List<OpsReleaseApply> selectOpsReleaseApplyByProcessInstanceIds(List<String> processInstanceIds);

    /**
     * Query Release Pending List
     *
     * @param userId User ID
     * @param roleIds Role IDs
     * @param apply Apply Query Params
     * @return Release Apply List
     */
    public List<OpsReleaseApply> selectReleasePendingList(@org.apache.ibatis.annotations.Param("userId") Long userId, 
                                                          @org.apache.ibatis.annotations.Param("userName") String userName,
                                                          @org.apache.ibatis.annotations.Param("roleIds") List<Long> roleIds,
                                                          @org.apache.ibatis.annotations.Param("apply") OpsReleaseApply apply);

    /**
     * Insert Release Apply
     * 
     * @param opsReleaseApply Release Apply
     * @return Result
     */
    public int insertOpsReleaseApply(OpsReleaseApply opsReleaseApply);

    /**
     * Update Release Apply
     * 
     * @param opsReleaseApply Release Apply
     * @return Result
     */
    public int updateOpsReleaseApply(OpsReleaseApply opsReleaseApply);

    /**
     * Delete Release Apply
     * 
     * @param id Release Apply ID
     * @return Result
     */
    public int deleteOpsReleaseApplyById(Long id);

    /**
     * Batch Delete Release Apply
     * 
     * @param ids Release Apply IDs
     * @return Result
     */
    public int deleteOpsReleaseApplyByIds(Long[] ids);
}
