package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsReleaseApply;

/**
 * Release Apply Service Interface
 * 
 * @author ruoyi
 */
public interface IOpsReleaseApplyService 
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
     * Delete Release Apply Information
     * 
     * @param id Release Apply ID
     * @return Result
     */
    public int deleteOpsReleaseApplyById(Long id);

    /**
     * Audit Release Apply
     * 
     * @param opsReleaseApply Release Apply
     * @return Result
     */
    public int auditOpsReleaseApply(OpsReleaseApply opsReleaseApply);

    /**
     * Process Approval (Pass/Reject)
     * 
     * @param id Release Apply ID
     * @param status Status (2-Pass, 3-Reject)
     * @param comment Audit Reason
     */
    public void processApproval(Long id, String status, String comment);

    /**
     * Execute Release
     * 
     * @param id Release Apply ID
     * @return Result
     */
    public void executeRelease(Long id);

    /**
     * Batch Delete Release Apply
     * 
     * @param ids Release Apply IDs
     * @return Result
     */
    public int deleteOpsReleaseApplyByIds(Long[] ids);

    /**
     * Query Pending Approvals for Current User
     *
     * @param query Query params
     * @return Release Apply List
     */
    public List<OpsReleaseApply> selectPendingApprovals(OpsReleaseApply query);
    
    /**
     * Super Admin One-Click Approval
     *
     * @param id Release Apply ID
     */
    public void superAdminApprove(Long id);

    /**
     * Cancel Release Apply (Withdraw)
     *
     * @param id Release Apply ID
     */
    public void cancelReleaseApply(Long id);
}
