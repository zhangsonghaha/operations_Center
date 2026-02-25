package com.ruoyi.quartz.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.system.domain.SysDbBackupStrategy;
import com.ruoyi.system.service.ISysDbBackupStrategyService;

/**
 * 数据库备份定时任务
 * 
 * @author ruoyi
 */
@Component("dbBackupTask")
public class DbBackupTask
{
    private static final Logger logger = LoggerFactory.getLogger(DbBackupTask.class);

    @Autowired
    private ISysDbBackupStrategyService strategyService;

    /**
     * 执行所有启用的备份策略
     */
    public void executeAllStrategies()
    {
        logger.info("开始执行定时备份任务");
        
        try {
            // 查询所有启用的策略
            List<SysDbBackupStrategy> strategies = strategyService.selectEnabledStrategyList();
            
            if (strategies == null || strategies.isEmpty())
            {
                logger.info("没有启用的备份策略");
                return;
            }

            logger.info("发现 {} 个启用的备份策略", strategies.size());

            for (SysDbBackupStrategy strategy : strategies)
            {
                try {
                    logger.info("执行备份策略: {} (ID: {})", strategy.getStrategyName(), strategy.getStrategyId());
                    strategyService.executeBackupByStrategy(strategy.getStrategyId());
                    logger.info("备份策略执行成功: {}", strategy.getStrategyName());
                } catch (Exception e) {
                    logger.error("备份策略执行失败: {} - {}", strategy.getStrategyName(), e.getMessage());
                }
            }

            logger.info("定时备份任务执行完成");
        } catch (Exception e) {
            logger.error("定时备份任务执行异常", e);
        }
    }

    /**
     * 根据策略ID执行单个策略
     * 
     * @param strategyId 策略ID
     */
    public void executeStrategy(String strategyId)
    {
        if (strategyId == null || strategyId.isEmpty())
        {
            logger.error("策略ID不能为空");
            return;
        }

        try {
            Long id = Long.parseLong(strategyId);
            logger.info("执行备份策略 ID: {}", id);
            strategyService.executeBackupByStrategy(id);
            logger.info("备份策略执行成功 ID: {}", id);
        } catch (NumberFormatException e) {
            logger.error("无效的策略ID: {}", strategyId);
        } catch (Exception e) {
            logger.error("备份策略执行失败 ID: {} - {}", strategyId, e.getMessage());
        }
    }

    /**
     * 清理过期备份
     */
    public void cleanExpiredBackups()
    {
        logger.info("开始清理过期备份");
        // 清理逻辑已在备份策略执行时自动处理
        logger.info("过期备份清理完成");
    }
}
