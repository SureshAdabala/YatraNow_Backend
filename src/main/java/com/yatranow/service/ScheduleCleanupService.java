package com.yatranow.service;

import com.yatranow.entity.Schedule;
import com.yatranow.repository.BookingRepository;
import com.yatranow.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleCleanupService {

    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;

    // Run daily at midnight (00:00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupPastSchedules() {
        LocalDate today = LocalDate.now();
        log.info("Starting cleanup of schedules before {}", today);

        try {
            // 1. Find past schedules
            List<Schedule> pastSchedules = scheduleRepository.findByScheduleDateBefore(today);

            if (pastSchedules.isEmpty()) {
                log.info("No past schedules found to cleanup.");
                return;
            }

            List<Long> pastScheduleIds = pastSchedules.stream()
                    .map(Schedule::getId)
                    .collect(Collectors.toList());

            // 2. Delete associated bookings first (to respect FK constraints if cascade
            // isn't automatic, and for safety)
            log.info("Deleting bookings associated with {} past schedules...", pastScheduleIds.size());
            bookingRepository.deleteByScheduleIdIn(pastScheduleIds);

            // 3. Delete the schedules
            log.info("Deleting {} past schedules...", pastScheduleIds.size());
            scheduleRepository.deleteAll(pastSchedules);

            log.info("Cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Error during schedule cleanup: {}", e.getMessage(), e);
        }
    }
}
