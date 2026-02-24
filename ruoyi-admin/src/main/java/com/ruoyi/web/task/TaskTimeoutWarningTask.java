package com.ruoyi.web.task;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 任务超时预警任务
 */
@Component
public class TaskTimeoutWarningTask {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ISysMessageService messageService;

    /**
     * 每小时执行一次，检查24小时内到期的任务
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkTaskTimeout() {
        Date now = new Date();
        Date future24h = DateUtils.addHours(now, 24);

        // 查询未来24小时内到期且未过期的任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskDueAfter(now)
                .taskDueBefore(future24h)
                .list();

        for (Task task : tasks) {
            String assignee = task.getAssignee();
            if (assignee != null) {
                try {
                    // 检查是否已发送预警（利用本地变量标记）
                    Boolean warned = (Boolean) taskService.getVariableLocal(task.getId(), "timeoutWarned");
                    if (warned == null || !warned) {
                        // 发送站内信
                        SysMessage msg = new SysMessage();
                        msg.setSenderId(1L); // 系统管理员ID
                        msg.setReceiverId(Long.valueOf(assignee));
                        msg.setTitle("【即将超时】任务预警");
                        msg.setContent("您的任务 [" + task.getName() + "] 将于 " + DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", task.getDueDate()) + " 截止，请尽快处理。");
                        msg.setMessageType("3"); // 3=催办/预警类型
                        msg.setBusinessId(task.getProcessInstanceId());
                        messageService.insertSysMessage(msg);
                        
                        // 标记已预警
                        taskService.setVariableLocal(task.getId(), "timeoutWarned", true);
                    }
                } catch (Exception e) {
                    // 忽略异常，避免中断整个任务
                }
            }
        }
    }
}
