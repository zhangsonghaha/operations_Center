package com.ruoyi.web.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.web.domain.OpsMonitorLog;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.mapper.OpsMonitorLogMapper;
import com.ruoyi.web.mapper.OpsServerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务器监控Service业务层处理
 */
@Service
public class OpsMonitorService
{
    private static final Logger log = LoggerFactory.getLogger(OpsMonitorService.class);

    @Autowired
    private OpsMonitorLogMapper opsMonitorLogMapper;
    
    @Autowired
    private OpsServerMapper opsServerMapper;

    /**
     * 查询服务器监控日志列表
     * 
     * @param opsMonitorLog 服务器监控日志
     * @return 服务器监控日志
     */
    public List<OpsMonitorLog> selectOpsMonitorLogList(OpsMonitorLog opsMonitorLog)
    {
        return opsMonitorLogMapper.selectOpsMonitorLogList(opsMonitorLog);
    }
    
    /**
     * 获取服务器实时监控数据
     */
    public OpsMonitorLog getRealtimeData(Long serverId) {
        OpsServer server = opsServerMapper.selectOpsServerByServerId(serverId);
        if (server == null) {
            throw new RuntimeException("服务器不存在");
        }
        
        return collectData(server);
    }
    
    /**
     * 采集单个服务器数据
     */
    public OpsMonitorLog collectData(OpsServer server) {
        OpsMonitorLog logData = new OpsMonitorLog();
        logData.setServerId(server.getServerId());
        logData.setCreateTime(new Date());
        
        Session session = null;
        try {
            JSch jsch = new JSch();
            if ("1".equals(server.getAuthType()) && server.getPrivateKey() != null) {
                jsch.addIdentity("key_" + server.getServerId(), server.getPrivateKey().getBytes(), null, null);
            }
            
            session = jsch.getSession(server.getUsername(), server.getPublicIp(), server.getServerPort());
            session.setConfig("StrictHostKeyChecking", "no");
            
            if ("0".equals(server.getAuthType())) {
                session.setPassword(server.getPassword());
            }
            
            session.connect(5000); // 5秒连接超时
            
            // 1. 获取 CPU 使用率 (top -bn1 | grep "Cpu(s)")
            // %Cpu(s):  0.7 us,  0.3 sy,  0.0 ni, 99.0 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
            String cpuCmd = "top -bn1 | grep \"Cpu(s)\"";
            String cpuOutput = executeCommand(session, cpuCmd);
            BigDecimal cpu = parseCpuUsage(cpuOutput);
            if (cpu.compareTo(BigDecimal.ZERO) == 0) {
                cpu = calcCpuUsageFromProcStat(session);
            }
            logData.setCpuUsage(cpu);
            
            // 2. 获取内存使用率 (free -m | grep Mem)
            // Mem:           7961        2534        2341          56        3085        5113
            String memCmd = "free -m | grep Mem";
            String memOutput = executeCommand(session, memCmd);
            BigDecimal mem = parseMemUsage(memOutput);
            if (mem.compareTo(BigDecimal.ZERO) == 0) {
                mem = calcMemUsageFromMeminfo(session);
            }
            logData.setMemoryUsage(mem);
            
            // 3. 获取磁盘使用率 (df -h / | tail -1)
            // /dev/vda1        40G   14G   24G  37% /
            String diskCmd = "df -h / | tail -1";
            String diskOutput = executeCommand(session, diskCmd);
            BigDecimal disk = parseDiskUsage(diskOutput);
            if (disk.compareTo(BigDecimal.ZERO) == 0) {
                String diskOutputPortable = executeCommand(session, "df -P / | awk 'NR==2 {print $5}'");
                disk = parseDiskUsage(diskOutputPortable);
            }
            logData.setDiskUsage(disk);
            
            // 4. 获取网络流量 (通过读取 /proc/net/dev 两次采样计算)
            // 采样间隔 1秒
            String netCmd = "cat /proc/net/dev; sleep 1; cat /proc/net/dev";
            String netOutput = executeCommand(session, netCmd);
            BigDecimal[] netRates = parseNetworkUsage(netOutput);
            logData.setNetTxRate(netRates[0]); // TX (Upload) KB/s
            logData.setNetRxRate(netRates[1]); // RX (Download) KB/s
            
        } catch (Exception e) {
            log.error("采集服务器监控数据失败: " + server.getServerName(), e);
            throw new RuntimeException("采集失败: " + e.getMessage());
        } finally {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        
        return logData;
    }
    
    /**
     * 批量采集所有服务器数据并入库（供定时任务调用）
     */
    public void collectTrendData() {
        OpsServer param = new OpsServer();
        param.setStatus("0"); // 只采集正常状态的服务器
        List<OpsServer> servers = opsServerMapper.selectOpsServerList(param);
        
        for (OpsServer server : servers) {
            try {
                OpsMonitorLog logData = collectData(server);
                opsMonitorLogMapper.insertOpsMonitorLog(logData);
            } catch (Exception e) {
                // 忽略单个采集失败，不影响其他
                log.error("定时采集失败: " + server.getServerName(), e);
            }
        }
        
        // 清理 7 天前的数据
        opsMonitorLogMapper.cleanExpiredLogs(7);
    }
    
    private String executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(System.err);
        
        InputStream in = channel.getInputStream();
        channel.connect();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        channel.disconnect();
        return output.toString().trim();
    }
    
    private BigDecimal parseCpuUsage(String output) {
        try {
            // %Cpu(s):  0.7 us,  0.3 sy,  0.0 ni, 99.0 id...
            // id is idle time. Usage = 100 - id
            Pattern pattern = Pattern.compile("(\\d+\\.\\d+)\\s+id");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                BigDecimal idle = new BigDecimal(matcher.group(1));
                return BigDecimal.valueOf(100).subtract(idle);
            }
        } catch (Exception e) {
            log.warn("解析CPU数据失败: " + output);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal parseMemUsage(String output) {
        try {
            // Mem: total used free shared buff/cache available
            // Mem: 7961 2534 2341 56 3085 5113
            String[] parts = output.trim().split("\\s+");
            if (parts.length >= 2) {
                double total = Double.parseDouble(parts[1]);
                double used = Double.parseDouble(parts[2]);
                if (total > 0) {
                    return BigDecimal.valueOf((used / total) * 100).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
            }
        } catch (Exception e) {
             log.warn("解析内存数据失败: " + output);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal parseDiskUsage(String output) {
        try {
            // /dev/vda1 40G 14G 24G 37% /
            // extract 37%
            Pattern pattern = Pattern.compile("(\\d+)%");
            Matcher matcher = pattern.matcher(output);
            if (matcher.find()) {
                return new BigDecimal(matcher.group(1));
            }
        } catch (Exception e) {
             log.warn("解析磁盘数据失败: " + output);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal[] parseNetworkUsage(String output) {
        // output contains two snapshots of /proc/net/dev separated by newline
        // We need to parse all non-loopback interfaces and sum them up
        BigDecimal txRate = BigDecimal.ZERO;
        BigDecimal rxRate = BigDecimal.ZERO;

        try {
            String[] lines = output.split("\n");
            // Find where the second snapshot starts (it repeats the header)
            int secondHeaderIndex = -1;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].contains("Inter-") || lines[i].contains("face")) {
                    secondHeaderIndex = i;
                    break;
                }
            }
            
            if (secondHeaderIndex == -1) return new BigDecimal[]{txRate, rxRate};
            
            // Map<InterfaceName, [RX, TX]>
            java.util.Map<String, Long[]> firstSnapshot = new java.util.HashMap<>();
            java.util.Map<String, Long[]> secondSnapshot = new java.util.HashMap<>();
            
            parseSnapshot(lines, 0, secondHeaderIndex, firstSnapshot);
            parseSnapshot(lines, secondHeaderIndex, lines.length, secondSnapshot);
            
            long totalRxDiff = 0;
            long totalTxDiff = 0;
            
            for (String iface : secondSnapshot.keySet()) {
                if (firstSnapshot.containsKey(iface)) {
                    Long[] v1 = firstSnapshot.get(iface);
                    Long[] v2 = secondSnapshot.get(iface);
                    // v2 - v1 is bytes per second (since we slept 1s)
                    long rxDiff = v2[0] - v1[0];
                    long txDiff = v2[1] - v1[1];
                    if (rxDiff >= 0) totalRxDiff += rxDiff;
                    if (txDiff >= 0) totalTxDiff += txDiff;
                }
            }
            
            // Convert bytes/s to KB/s
            rxRate = BigDecimal.valueOf(totalRxDiff).divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
            txRate = BigDecimal.valueOf(totalTxDiff).divide(BigDecimal.valueOf(1024), 2, BigDecimal.ROUND_HALF_UP);
            
        } catch (Exception e) {
            log.warn("解析网络数据失败", e);
        }
        
        return new BigDecimal[]{txRate, rxRate};
    }
    
    private void parseSnapshot(String[] lines, int start, int end, java.util.Map<String, Long[]> snapshot) {
        for (int i = start; i < end; i++) {
            String line = lines[i].trim();
            if (line.contains(":") && !line.startsWith("lo")) { // skip loopback
                // eth0: 123 456 ...
                String[] parts = line.split(":");
                String iface = parts[0].trim();
                String[] stats = parts[1].trim().split("\\s+");
                if (stats.length >= 9) {
                    // stats[0] is RX bytes, stats[8] is TX bytes (usually)
                    // /proc/net/dev format:
                    // face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
                    //         0        1       2    3    4    5     6          7         8        9       10   11   12   13    14      15
                    try {
                        long rx = Long.parseLong(stats[0]);
                        long tx = Long.parseLong(stats[8]);
                        snapshot.put(iface, new Long[]{rx, tx});
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }
    
    /**
     * 通过 /proc/stat 计算 CPU 使用率，避免 top 本地化导致解析失败
     */
    private BigDecimal calcCpuUsageFromProcStat(Session session) {
        try {
            String stat = executeCommand(session, "cat /proc/stat | grep '^cpu ' ; sleep 1 ; cat /proc/stat | grep '^cpu '");
            String[] lines = stat.split("\\n");
            if (lines.length >= 2) {
                long[] v1 = parseCpuLine(lines[0]);
                long[] v2 = parseCpuLine(lines[1]);
                long idle1 = v1[3] + v1[4]; // idle + iowait
                long idle2 = v2[3] + v2[4];
                long total1 = 0, total2 = 0;
                for (long x : v1) total1 += x;
                for (long x : v2) total2 += x;
                long totalDiff = total2 - total1;
                long idleDiff = idle2 - idle1;
                if (totalDiff > 0) {
                    double usage = (1.0 - (idleDiff * 1.0 / totalDiff)) * 100.0;
                    return BigDecimal.valueOf(usage).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
            }
        } catch (Exception e) {
            log.warn("通过/proc/stat计算CPU失败", e);
        }
        return BigDecimal.ZERO;
    }
    
    private long[] parseCpuLine(String line) {
        // cpu  user nice system idle iowait irq softirq steal guest guest_nice
        String[] parts = line.trim().split("\\s+");
        // parts[0] = "cpu"
        int n = Math.max(11, parts.length - 1);
        long[] vals = new long[n];
        for (int i = 1; i < parts.length; i++) {
            try {
                vals[i - 1] = Long.parseLong(parts[i]);
            } catch (NumberFormatException e) {
                vals[i - 1] = 0L;
            }
        }
        return vals;
    }
    
    /**
     * 通过 /proc/meminfo 计算内存使用率，避免 free 命令差异
     */
    private BigDecimal calcMemUsageFromMeminfo(Session session) {
        try {
            String meminfo = executeCommand(session, "cat /proc/meminfo | egrep '^MemTotal|^MemAvailable'");
            long total = 0;
            long available = 0;
            String[] lines = meminfo.split("\\n");
            for (String l : lines) {
                if (l.startsWith("MemTotal")) {
                    total = parseMeminfoValue(l);
                } else if (l.startsWith("MemAvailable")) {
                    available = parseMeminfoValue(l);
                }
            }
            if (total > 0) {
                double usedPercent = ((total - available) * 100.0) / total;
                return BigDecimal.valueOf(usedPercent).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        } catch (Exception e) {
            log.warn("通过/proc/meminfo计算内存失败", e);
        }
        return BigDecimal.ZERO;
    }
    
    private long parseMeminfoValue(String line) {
        // e.g. MemTotal:       16367492 kB
        String[] ps = line.split("\\s+");
        for (String p : ps) {
            try {
                return Long.parseLong(p); // kB
            } catch (NumberFormatException ignored) {}
        }
        return 0L;
    }
}
