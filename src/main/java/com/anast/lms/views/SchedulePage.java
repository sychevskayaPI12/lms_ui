package com.anast.lms.views;


import com.anast.lms.model.SchedulerItem;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.model.WeekScheduler;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.tabs.PagedTabs;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Route(value = "/my_schedule", layout=MainLayout.class)
@PageTitle("Главная | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class SchedulePage extends VerticalLayout {

    private final ProfileServiceClient profileClient;
    private final SecurityService securityService;
    private final StudyServiceClient studyClient;

    private final Locale RuLocale;

    public SchedulePage(ProfileServiceClient profileClient, SecurityService securityService, StudyServiceClient studyClient) {
        this.profileClient = profileClient;
        this.securityService = securityService;
        this.studyClient = studyClient;
        this.RuLocale = new Locale("ru", "RU");

        build();
    }

    private void build() {

        UserDetails userDetails = securityService.getAuthenticatedUser();
        UserProfileInfo profileInfo = profileClient.getUserProfileInfo(userDetails.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        setPadding(true);
        setSpacing(true);
       /* Label header = new Label();
        header.setWidthFull();
        header.setHeight("3%");
        header.setText("Расписание на неделю");
        header.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(header);*/

        if(roles.containsAll(Arrays.asList("ROLE_TEACHER", "ROLE_STUDENT"))) {

            VerticalLayout container = new VerticalLayout();
            PagedTabs tabs = new PagedTabs(container);
            tabs.add("Расписание студента", buildStudentScheduleLayout(profileInfo), false);
            tabs.add("Расписание преподавателя", buildTeacherScheduleLayout(profileInfo), false);
            add(tabs, container);

        } else if(roles.contains("ROLE_TEACHER")) {
            add(new Label("Расписание преподавателя"));
            add(buildTeacherScheduleLayout(profileInfo));

        } else if(roles.contains("ROLE_STUDENT")) {
            add(new Label("Расписание студента"));

            add(buildStudentScheduleLayout(profileInfo));
        }
        setSpacing(false);
    }

    private VerticalLayout buildStudentScheduleLayout(UserProfileInfo profileInfo) {
        VerticalLayout layout = new VerticalLayout();
        WeekScheduler scheduler = studyClient.getStudentScheduler(profileInfo.getStudentInfo().getGroupCode(), false);

        for(short dayOfWeek = 1; dayOfWeek< 6; dayOfWeek++) {
            VerticalLayout dayLayout = getDailyLayoutStyled(dayOfWeek);
            List<SchedulerItem> daily = scheduler.getWeekClasses().get(dayOfWeek);

            if(daily == null || daily.isEmpty()) {
                dayLayout.add(getNoClassesLabel());
            } else {
                daily.sort(Comparator.comparing(SchedulerItem::getNumber));
                dayLayout.add(StudyUtils.getStudentDailyGrid(daily));
            }

            layout.add(dayLayout);
        }


        return layout;
    }

    private VerticalLayout buildTeacherScheduleLayout(UserProfileInfo profileInfo) {
        VerticalLayout layout = new VerticalLayout();
        WeekScheduler scheduler = studyClient.getTeacherScheduler(profileInfo.getLogin(), false);

        for(short dayOfWeek = 1; dayOfWeek< 6; dayOfWeek++) {
            VerticalLayout dayLayout = getDailyLayoutStyled(dayOfWeek);
            List<SchedulerItem> daily = scheduler.getWeekClasses().get(dayOfWeek);

            if(daily == null || daily.isEmpty()) {
                dayLayout.add(getNoClassesLabel());
            } else {
                daily.sort(Comparator.comparing(SchedulerItem::getNumber));
                dayLayout.add(StudyUtils.getTeacherDailyGrid(daily));
            }

            layout.add(dayLayout);
        }

        return layout;
    }

    private Label getNoClassesLabel() {
        Label label = new Label("Нет занятий");
        label.getStyle().set("font-size", "var(--lumo-font-size-s)");
        return label;
    }

    private VerticalLayout getDailyLayoutStyled(short dayOfWeek) {
        VerticalLayout dayLayout = new VerticalLayout();
        dayLayout.add(new Label(DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL_STANDALONE, RuLocale) + ":"));

        dayLayout.getStyle().set("border", "2px solid lightsteelblue")
                .set("border-radius", "var(--lumo-border-radius-s)");
        dayLayout.setWidth("80%");
        return dayLayout;
    }

}
