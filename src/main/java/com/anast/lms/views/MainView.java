package com.anast.lms.views;

import com.anast.lms.model.SchedulerItem;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.model.WeekScheduler;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Route(layout=MainLayout.class)
@PageTitle("Главная | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private final ProfileServiceClient profileClient;
    private final SecurityService securityService;
    private final StudyServiceClient studyClient;


    public MainView(ProfileServiceClient profileClient, SecurityService securityService, StudyServiceClient studyClient) {
        this.profileClient = profileClient;
        this.securityService = securityService;
        this.studyClient = studyClient;
        build();
    }

    private void build() {

        String login = securityService.getAuthenticatedUser().getUsername();
        UserProfileInfo profileInfo = profileClient.getUserProfileInfo(login);

        setPadding(true);
        setSpacing(true);
        Label header = new Label();
        header.setWidthFull();
        header.setHeight("3%");
        header.setText(String.format("Добро пожаловать, %s!", profileInfo.getFullName()));
        header.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(header);

        addInfoLayout(profileInfo);
        addCurrentScheduleLayout(profileInfo);

    }

    private void addInfoLayout(UserProfileInfo profileInfo) {
        HorizontalLayout infoLayout = new HorizontalLayout();
        if(profileInfo.getTeacherInfo() == null && profileInfo.getStudentInfo() == null) {
            //todo
            infoLayout.add(new Button("Добавить информацию"));
        } else {

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
            if(profileInfo.getTeacherInfo() != null) {

                VerticalLayout teacherHelloLayout = new VerticalLayout();
                teacherHelloLayout.add(new Label(profileInfo.getTeacherInfo().getDegree()));
                teacherHelloLayout.getStyle()
                        //lavenderblush
                        .set("background-color", "lavender")
                        .set("border-radius", "var(--lumo-border-radius-s)");

                infoLayout.add(teacherHelloLayout);
            }
        }
        add(infoLayout);
    }

    private void addCurrentScheduleLayout(UserProfileInfo profileInfo) {

        Short dayOfWeek = (short) LocalDate.now().getDayOfWeek().getValue();
        if(profileInfo.getTeacherInfo() != null) {

            VerticalLayout teacherDailyLayout = new VerticalLayout();
            teacherDailyLayout.add(new Label("Занятия преподавателя на сегодня:"));

            WeekScheduler scheduler = studyClient.getTeacherScheduler(profileInfo.getLogin(), true);
            List<SchedulerItem> currentClasses = scheduler.getWeekClasses().get(dayOfWeek);
            if(currentClasses == null || currentClasses.isEmpty()) {
                teacherDailyLayout.add(new Label("Занятий нет"));
            } else {
                currentClasses.sort(Comparator.comparing(SchedulerItem::getNumber));
                teacherDailyLayout.add(getTeacherDailyGrid(currentClasses));
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
                studentDailyLayout.add(getStudentDailyGrid(currentClasses));
            }
            add(studentDailyLayout);
        }
    }

    private Grid<SchedulerItem> getStudentDailyGrid(List<SchedulerItem> items) {
        Grid<SchedulerItem> grid = new Grid<>(SchedulerItem.class, false);
        grid.addColumn(SchedulerItem::getNumber).setHeader("Пара").setAutoWidth(true);
        grid.addColumn(item -> item.getDiscipline().getTitle()).setHeader("Дисциплина")
                .setAutoWidth(true);
        grid.addColumn(item -> item.getClassType().getTitle()).setAutoWidth(true);
        grid.addColumn(SchedulerItem::getClassRoom).setHeader("Аудитория").setAutoWidth(true);


        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setAllRowsVisible(true);
        grid.setWidth("75%");
        grid.setItems(items);
        return grid;
    }

    private Grid<SchedulerItem> getTeacherDailyGrid(List<SchedulerItem> items) {
        Grid<SchedulerItem> grid = new Grid<>(SchedulerItem.class, false);
        grid.addColumn(SchedulerItem::getNumber).setHeader("Пара").setAutoWidth(true);
        grid.addColumn(item -> item.getDiscipline().getTitle()).setHeader("Дисциплина")
                .setAutoWidth(true);
        grid.addColumn(item -> item.getClassType().getTitle()).setAutoWidth(true);
        grid.addColumn(SchedulerItem::getGroups).setHeader("Группы").setAutoWidth(true);
        grid.addColumn(SchedulerItem::getClassRoom).setHeader("Аудитория").setAutoWidth(true);


        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setAllRowsVisible(true);
        grid.setWidth("75%");
        grid.setItems(items);
        return grid;
    }
}
