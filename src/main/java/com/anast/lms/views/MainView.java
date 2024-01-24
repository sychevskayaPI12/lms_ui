package com.anast.lms.views;

import com.anast.lms.model.RequestState;
import com.anast.lms.model.SchedulerItem;
import com.anast.lms.model.WeekScheduler;
import com.anast.lms.model.profile.RegistrationRequest;
import com.anast.lms.model.profile.UserProfile;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.ModerationServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(layout=MainLayout.class)
@PageTitle("Главная | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private final SecurityService securityService;
    private final StudyServiceClient studyClient;
    private final ModerationServiceClient moderationClient;


    public MainView(SecurityService securityService, StudyServiceClient studyClient, ModerationServiceClient moderationClient) {
        this.securityService = securityService;
        this.studyClient = studyClient;
        this.moderationClient = moderationClient;
        build();
    }

    private void build() {

        String login = securityService.getAuthenticatedUser().getUsername();
        UserProfile profileInfo = studyClient.getUserProfileInfo(login);

        setPadding(true);
        setSpacing(true);
        Label header = new Label();
        header.setWidthFull();
        header.setHeight("3%");
        header.setText(String.format("Добро пожаловать, %s!", profileInfo.getUserProfileInfo().getFullName()));
        header.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(header);

        addInfoLayout(profileInfo);
        addCurrentScheduleLayout(profileInfo);

    }

    private void addInfoLayout(UserProfile profileInfo) {
        HorizontalLayout infoLayout = new HorizontalLayout();
        add(infoLayout);

        List<String> roles = securityService.getAuthenticatedUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        if(roles.contains("ROLE_MODERATOR")) {
            addModeratorLabel(infoLayout);
            addRequestsCount();
        }
        if(roles.contains("ROLE_STUDENT")) {
            addStudentLabel(infoLayout, profileInfo);
        }
        if(roles.contains("ROLE_TEACHER")) {
            addTeacherLabel(infoLayout, profileInfo);
        }
    }

    private void addRequestsCount() {

        List<RegistrationRequest> requests = moderationClient.getRegistrationRequests(RequestState.unprocessed.getValue());
        int count = requests != null ? requests.size() : 0;
        Label countLabel = new Label("Новых заявок: " + count);
        RouterLink link = new RouterLink("Перейти к управлению заявками", RegistrationRequestsPage.class);

        VerticalLayout requestCountLayout = new VerticalLayout(countLabel, link);
        requestCountLayout.setPadding(true);
        add(requestCountLayout);
    }

    private void addStudentLabel(HorizontalLayout infoLayout, UserProfile profileInfo) {
        if(profileInfo.getStudentInfo() != null) {
            VerticalLayout studentHelloLayout = new VerticalLayout();
            Label label1 = new Label(String.format("Студент %s курса",
                    profileInfo.getStudentInfo().getCourse()));
            label1.getStyle().set("width", "max-content");
            Label label2 = new Label(String.format("Группа %s", profileInfo.getStudentInfo().getGroupCode()));
            label2.getStyle().set("width", "max-content");
            studentHelloLayout.add(label1, label2);
            studentHelloLayout.getStyle()
                    .set("background-color", "lavenderblush")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("width", "max-content");
            studentHelloLayout.setSpacing(false);

            infoLayout.add(studentHelloLayout);
        }
    }

    private void addTeacherLabel(HorizontalLayout infoLayout, UserProfile profileInfo) {
        if(profileInfo.getTeacherInfo() != null) {

            VerticalLayout teacherHelloLayout = new VerticalLayout();
            profileInfo.getTeacherInfo().getPositions().forEach(pos -> {
                Label posLabel = new Label(pos.toString());
                teacherHelloLayout.add(posLabel);
            });
            teacherHelloLayout.getStyle()
                    //lavenderblush
                    .set("background-color", "lavender")
                    .set("border-radius", "var(--lumo-border-radius-s)");

            infoLayout.add(teacherHelloLayout);
        }
    }

    private void addModeratorLabel(HorizontalLayout infoLayout) {
        VerticalLayout moderatorHelloLayout = new VerticalLayout();
        Label label = new Label("Модератор");
        moderatorHelloLayout.add(label);
        moderatorHelloLayout.getStyle()
                //lavenderblush
                .set("background-color", "lavender")
                .set("border-radius", "var(--lumo-border-radius-s)");

        infoLayout.add(moderatorHelloLayout);
    }

    private void addCurrentScheduleLayout(UserProfile profileInfo) {

        Short dayOfWeek = (short) LocalDate.now().getDayOfWeek().getValue();
        if(profileInfo.getTeacherInfo() != null) {

            VerticalLayout teacherDailyLayout = new VerticalLayout();
            teacherDailyLayout.add(new Label("Занятия преподавателя на сегодня:"));

            WeekScheduler scheduler = studyClient.getTeacherScheduler(profileInfo.getUserProfileInfo().getLogin(), true);
            List<SchedulerItem> currentClasses = scheduler.getWeekClasses().get(dayOfWeek);
            if(currentClasses == null || currentClasses.isEmpty()) {
                teacherDailyLayout.add(new Label("Занятий нет"));
            } else {
                currentClasses.sort(Comparator.comparing(SchedulerItem::getNumber));
                Grid<SchedulerItem> grid = StudyUtils.getTeacherDailyGrid(currentClasses);
                grid.setWidth("75%");
                teacherDailyLayout.add(grid);
            }
            add(teacherDailyLayout);
        }

        if(profileInfo.getStudentInfo() != null) {

            VerticalLayout studentDailyLayout = new VerticalLayout();
            studentDailyLayout.add(new Label("Занятия студента на сегодня:"));

            WeekScheduler scheduler = studyClient.getStudentScheduler(profileInfo.getStudentInfo().getGroupCode(), true);
            List<SchedulerItem> currentClasses = scheduler.getWeekClasses().get(dayOfWeek);
            if(currentClasses == null || currentClasses.isEmpty()) {
                studentDailyLayout.add(new Label("Занятий нет"));
            } else {
                currentClasses.sort(Comparator.comparing(SchedulerItem::getNumber));
                Grid<SchedulerItem> grid = StudyUtils.getStudentDailyGrid(currentClasses);
                grid.setWidth("75%");
                studentDailyLayout.add(grid);
            }
            add(studentDailyLayout);
        }
    }
}
