package com.kolaysoft.ctotracker.config;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kolaysoft.ctotracker.entity.OverallStatus;
import com.kolaysoft.ctotracker.entity.ProgressStage;
import com.kolaysoft.ctotracker.entity.Project;
import com.kolaysoft.ctotracker.entity.ProjectStatus;
import com.kolaysoft.ctotracker.entity.RiskLevel;
import com.kolaysoft.ctotracker.entity.Role;
import com.kolaysoft.ctotracker.entity.User;
import com.kolaysoft.ctotracker.entity.WeeklyReport;
import com.kolaysoft.ctotracker.repository.ProjectRepository;
import com.kolaysoft.ctotracker.repository.UserRepository;
import com.kolaysoft.ctotracker.repository.WeeklyReportRepository;

/**
 * Gelistirme/demo icin ornek veri yukler. Bellekteki H2 her baslatmada bos geldigi
 * icin uygulama acilisinda birkac kullanici, proje ve haftalik rapor olusturur.
 * Idempotent: veri zaten varsa tekrar eklemez.
 *
 * <p>Gercek/uretim ortaminda calismasi istenmiyorsa profile ile sinirlandirilabilir.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    public DataSeeder(UserRepository userRepository, ProjectRepository projectRepository,
                      WeeklyReportRepository weeklyReportRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.weeklyReportRepository = weeklyReportRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User ayse = saveUser("ayse@kolaysoft.com", "Ayse Yilmaz", Role.PROJECT_MANAGER);
        User mehmet = saveUser("mehmet@kolaysoft.com", "Mehmet Kaya", Role.PROJECT_MANAGER);
        saveUser("cto@kolaysoft.com", "Zeynep Demir", Role.CTO);
        saveUser("admin@kolaysoft.com", "Sistem Admin", Role.ADMIN);

        Project peyk = saveProject("PEYK", "Kolaysoft", "Insan kaynaklari surecleri urunu",
                ProjectStatus.DEVAM_EDIYOR, ayse);
        Project edonusum = saveProject("e-Donusum", "Kolaysoft", "e-Fatura / e-Arsiv cozumleri",
                ProjectStatus.DEVAM_EDIYOR, mehmet);
        saveProject("EczaciPOS", "Kolaysoft", "Eczanelere POS ve yazilim cozumu",
                ProjectStatus.PLANLANDI, ayse);

        saveReport(peyk, 29, LocalDate.of(2026, 7, 20), ProgressStage.GELISTIRME,
                OverallStatus.YOLUNDA, RiskLevel.ORTA,
                "Rapor formu API'si gelistirildi.", "Dashboard ozet endpoint'i.",
                "Zaman kisiti.", "Genel gidisat iyi.");
        saveReport(peyk, 30, LocalDate.of(2026, 7, 27), ProgressStage.TEST,
                OverallStatus.YOLUNDA, RiskLevel.DUSUK,
                "Testler yazildi.", "Hata duzeltme.", "-", "Test asamasinda.");
        saveReport(edonusum, 30, LocalDate.of(2026, 7, 27), ProgressStage.ANALIZ,
                OverallStatus.RISKLI, RiskLevel.YUKSEK,
                "Mevzuat analizi.", "Veri modeli.", "Mevzuat belirsizligi.", "Analiz suruyor.");

        log.info("Ornek veri yuklendi: {} kullanici, {} proje, {} rapor",
                userRepository.count(), projectRepository.count(), weeklyReportRepository.count());
    }

    private User saveUser(String email, String fullName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        return userRepository.save(user);
    }

    private Project saveProject(String name, String customer, String description,
                                ProjectStatus status, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setCustomer(customer);
        project.setDescription(description);
        project.setStatus(status);
        project.setOwner(owner);
        return projectRepository.save(project);
    }

    private void saveReport(Project project, int weekNumber, LocalDate reportDate, ProgressStage stage,
                            OverallStatus overallStatus, RiskLevel riskLevel,
                            String done, String planned, String risks, String note) {
        WeeklyReport report = new WeeklyReport();
        report.setProject(project);
        report.setWeekNumber(weekNumber);
        report.setReportDate(reportDate);
        report.setProgressStage(stage);
        report.setOverallStatus(overallStatus);
        report.setRiskLevel(riskLevel);
        report.setDone(done);
        report.setPlanned(planned);
        report.setRisks(risks);
        report.setNote(note);
        weeklyReportRepository.save(report);
    }
}
