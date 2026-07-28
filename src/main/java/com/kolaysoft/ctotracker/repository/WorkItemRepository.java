package com.kolaysoft.ctotracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kolaysoft.ctotracker.entity.WorkItem;
import com.kolaysoft.ctotracker.entity.WorkItemStatus;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {

    /** Bir haftalık rapora bağlı iş kalemleri. */
    List<WorkItem> findByWeeklyReportId(Long weeklyReportId);

    /** Canlı task hesabı için: bir raporda belirli durumdaki iş kalemi sayısı (on analiz H-05). */
    long countByWeeklyReportIdAndStatus(Long weeklyReportId, WorkItemStatus status);
}
