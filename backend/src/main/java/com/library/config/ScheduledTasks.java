package com.library.config;

import com.library.service.BorrowService;
import com.library.service.ReserveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：逾期转购买 + 预约失效处理
 */
@Slf4j
@Component
public class ScheduledTasks {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private ReserveService reserveService;

    /**
     * 每天凌晨 1 点扫描逾期未还记录
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processOverdue() {
        log.info("开始执行逾期转购买定时任务...");
        try {
            borrowService.processOverdue();
            log.info("逾期转购买定时任务执行完成");
        } catch (Exception e) {
            log.error("逾期转购买定时任务执行失败", e);
        }
    }

    /**
     * 每小时扫描一次失效预约（顺延下一位）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processExpiredReserve() {
        log.info("开始执行预约失效处理定时任务...");
        try {
            reserveService.processExpired();
            log.info("预约失效处理完成");
        } catch (Exception e) {
            log.error("预约失效处理失败", e);
        }
    }
}
