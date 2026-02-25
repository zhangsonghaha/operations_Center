package com.ruoyi.system.service.backup;

/**
 * 恢复进度回调接口
 * 
 * @author ruoyi
 */
public interface RestoreProgressCallback {
    
    /**
     * 更新进度
     * 
     * @param progress 进度百分比 0-100
     * @param message 进度消息
     */
    void onProgress(int progress, String message);
    
    /**
     * 更新当前步骤
     * 
     * @param step 步骤名称
     */
    void onStep(String step);
    
    /**
     * 添加详细日志
     * 
     * @param log 日志内容
     */
    void onLog(String log);
    
    /**
     * 更新统计信息
     * 
     * @param totalTables 总表数
     * @param completedTables 已完成表数
     */
    void onTableProgress(int totalTables, int completedTables);
    
    /**
     * 更新影响范围
     * 
     * @param scope 影响范围描述
     */
    void onScope(String scope);
    
    /**
     * 完成回调
     * 
     * @param success 是否成功
     * @param message 完成消息
     */
    void onComplete(boolean success, String message);
}
