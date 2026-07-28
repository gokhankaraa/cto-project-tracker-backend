package com.kolaysoft.ctotracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kolaysoft.ctotracker.common.exception.ResourceInUseException;
import com.kolaysoft.ctotracker.common.exception.ResourceNotFoundException;
import com.kolaysoft.ctotracker.dto.ProjectRequest;
import com.kolaysoft.ctotracker.dto.ProjectResponse;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.ProjectStatus;
import com.kolaysoft.ctotracker.entity.Role;
import com.kolaysoft.ctotracker.entity.User;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.UserRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;

/** ProjectService birim testleri (Spring context'siz, Mockito ile). */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WeeklyReportRepository weeklyReportRepository;

    @InjectMocks
    private ProjectService service;

    private User owner() {
        User u = new User();
        u.setId(1L);
        u.setEmail("ayse@kolaysoft.com");
        u.setFullName("Ayse Yilmaz");
        u.setRole(Role.PROJECT_MANAGER);
        return u;
    }

    @Test
    @DisplayName("Olusturma: gecerli owner ile proje olusur ve owner ozeti doner")
    void createMapsFieldsAndOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(projectRepository.save(any())).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            p.setId(7L);
            return p;
        });

        ProjectResponse response = service.create(
                new ProjectRequest("PEYK", "Kolaysoft", "aciklama", ProjectStatus.DEVAM_EDIYOR, 1L));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.name()).isEqualTo("PEYK");
        assertThat(response.status()).isEqualTo(ProjectStatus.DEVAM_EDIYOR);
        assertThat(response.ownerId()).isEqualTo(1L);
        assertThat(response.ownerFullName()).isEqualTo("Ayse Yilmaz");
    }

    @Test
    @DisplayName("Olusturma: olmayan owner 404 ResourceNotFound")
    void createRejectsMissingOwner() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(
                new ProjectRequest("PEYK", "Kolaysoft", "aciklama", ProjectStatus.DEVAM_EDIYOR, 99L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Detay: olmayan proje 404 ResourceNotFound")
    void findByIdRejectsMissingProject() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Silme: olmayan proje 404 ResourceNotFound")
    void deleteRejectsMissingProject() {
        when(projectRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Silme: raporu olan proje silinemez, 409 ResourceInUse")
    void deleteRejectsProjectWithReports() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(weeklyReportRepository.existsByProjectId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ResourceInUseException.class);

        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Silme: raporu olmayan proje silinir")
    void deleteRemovesProjectWithoutReports() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(weeklyReportRepository.existsByProjectId(1L)).thenReturn(false);

        service.delete(1L);

        verify(projectRepository).deleteById(1L);
    }
}
