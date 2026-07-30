package com.kolaysoft.ctotracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kolaysoft.ctotracker.common.exception.ResourceNotFoundException;
import com.kolaysoft.ctotracker.dto.WorkItemRequest;
import com.kolaysoft.ctotracker.dto.WorkItemResponse;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.entity.WorkItem;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;
import com.kolaysoft.ctotracker.repository.WorkItemRepository;

/**
 * İş kalemi CRUD is mantigi (on analiz H-05).
 * Her is kalemi bir haftalik rapora baglidir; baglanti URL yolundan gelir.
 */
@Service
@Transactional
public class WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    public WorkItemService(WorkItemRepository workItemRepository,
                           WeeklyReportRepository weeklyReportRepository) {
        this.workItemRepository = workItemRepository;
        this.weeklyReportRepository = weeklyReportRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkItemResponse> findByReport(Long reportId) {
        requireReportExists(reportId);
        return workItemRepository.findByWeeklyReportId(reportId).stream()
                .map(WorkItemService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkItemResponse findById(Long workItemId) {
        return toResponse(getWorkItemOrThrow(workItemId));
    }

    public WorkItemResponse create(Long reportId, WorkItemRequest request) {
        WeeklyReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Haftalik rapor", reportId));

        WorkItem workItem = new WorkItem();
        workItem.setWeeklyReport(report);
        applyRequest(workItem, request);
        return toResponse(workItemRepository.save(workItem));
    }

    public WorkItemResponse update(Long workItemId, WorkItemRequest request) {
        WorkItem workItem = getWorkItemOrThrow(workItemId);
        applyRequest(workItem, request);
        return toResponse(workItemRepository.save(workItem));
    }

    public void delete(Long workItemId) {
        if (!workItemRepository.existsById(workItemId)) {
            throw new ResourceNotFoundException("Is kalemi", workItemId);
        }
        workItemRepository.deleteById(workItemId);
    }

    private void applyRequest(WorkItem workItem, WorkItemRequest request) {
        workItem.setTitle(request.title());
        workItem.setDescription(request.description());
        workItem.setAssignee(request.assignee());
        workItem.setStatus(request.status());
        workItem.setPlannedDate(request.plannedDate());
        workItem.setCompletedDate(request.completedDate());
    }

    private WorkItem getWorkItemOrThrow(Long workItemId) {
        return workItemRepository.findById(workItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Is kalemi", workItemId));
    }

    private void requireReportExists(Long reportId) {
        if (!weeklyReportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Haftalik rapor", reportId);
        }
    }

    private static WorkItemResponse toResponse(WorkItem w) {
        return new WorkItemResponse(
                w.getId(),
                w.getWeeklyReport().getId(),
                w.getTitle(),
                w.getDescription(),
                w.getAssignee(),
                w.getStatus(),
                w.getPlannedDate(),
                w.getCompletedDate());
    }
}
