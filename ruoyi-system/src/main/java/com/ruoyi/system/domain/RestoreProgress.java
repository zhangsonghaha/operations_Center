package com.ruoyi.system.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RestoreProgress {

    private static final Map<String, RestoreProgress> INSTANCES = new ConcurrentHashMap<>();

    private String taskId;
    private int totalTables;
    private AtomicInteger completedTables = new AtomicInteger(0);
    private int totalRecords;
    private AtomicInteger completedRecords = new AtomicInteger(0);
    private String status;
    private String currentTable;
    private String startTime;
    private String endTime;
    private String errorMessage;
    private List<String> logs = new ArrayList<>();
    private String backupFileName;
    private String targetDatabase;

    public static RestoreProgress create(String taskId) {
        RestoreProgress progress = new RestoreProgress();
        progress.taskId = taskId;
        progress.status = "running";
        progress.startTime = java.time.LocalDateTime.now().toString();
        INSTANCES.put(taskId, progress);
        return progress;
    }

    public static RestoreProgress get(String taskId) {
        return INSTANCES.get(taskId);
    }

    public static void remove(String taskId) {
        INSTANCES.remove(taskId);
    }

    public int getProgress() {
        if (totalTables > 0) {
            return (completedTables.get() * 100) / totalTables;
        } else if (totalRecords > 0) {
            return (completedRecords.get() * 100) / totalRecords;
        }
        return 0;
    }

    public void addLog(String log) {
        String timestamp = java.time.LocalDateTime.now().toString();
        logs.add("[" + timestamp + "] " + log);
    }

    public void setComplete() {
        this.status = "completed";
        this.endTime = java.time.LocalDateTime.now().toString();
    }

    public void setFailed(String error) {
        this.status = "failed";
        this.errorMessage = error;
        this.endTime = java.time.LocalDateTime.now().toString();
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public int getTotalTables() { return totalTables; }
    public void setTotalTables(int totalTables) { this.totalTables = totalTables; }

    public int getCompletedTables() { return completedTables.get(); }
    public void incrementCompletedTables() { completedTables.incrementAndGet(); }

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }

    public int getCompletedRecords() { return completedRecords.get(); }
    public void incrementCompletedRecords() { completedRecords.incrementAndGet(); }

    public void addCompletedRecords(int count) { completedRecords.addAndGet(count); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCurrentTable() { return currentTable; }
    public void setCurrentTable(String currentTable) { this.currentTable = currentTable; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public List<String> getLogs() { return logs; }

    public String getBackupFileName() { return backupFileName; }
    public void setBackupFileName(String backupFileName) { this.backupFileName = backupFileName; }

    public String getTargetDatabase() { return targetDatabase; }
    public void setTargetDatabase(String targetDatabase) { this.targetDatabase = targetDatabase; }
}
