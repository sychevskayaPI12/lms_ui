package com.anast.lms.views;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Route(value = "/my_courses/teacher")
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class TeachersCoursesView extends VerticalLayout {

    private final StudyServiceClient studyClient;
    private final UserProfileInfo profileInfo;

    private VerticalLayout coursesListLayout;
    private Select<String> select;
    private Select<String> specialtySelect;


    public TeachersCoursesView(StudyServiceClient studyClient, UserProfileInfo profileInfo) {
        this.studyClient = studyClient;
        this.profileInfo = profileInfo;

        setWidthFull();
        setHeightFull();
        build();
    }

    private void build() {

        if(profileInfo.getTeacherInfo() == null) {
            return;
        }

        HorizontalLayout filterLayout = new HorizontalLayout();

        select = new Select<>();
        select.setItems(Arrays.stream(CourseSearchType.values())
                .map(CourseSearchType::getDisplay).collect(Collectors.toList()));
        select.setValue(CourseSearchType.ACTIVE.getDisplay());
        select.addValueChangeListener(e -> selectChangeListener());

        specialtySelect = new Select<>();
        specialtySelect.setPlaceholder("Направление");
        specialtySelect.setItems(studyClient.getSpecialties());

        Button searchButton = new Button("Найти", new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> searchCoursesEventListener());

        Button clearButton = new Button("Сбросить");
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearButtonListener());

        //todo filters fields + button
        filterLayout.add(select, specialtySelect, searchButton, clearButton);

        coursesListLayout = new VerticalLayout();
        coursesListLayout.setPadding(false);

        fillCoursesList(true);

        Scroller scroller = new Scroller(coursesListLayout);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidth("80%");
        scroller.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "var(--lumo-space-m)");

        add(filterLayout, scroller);

    }

    private void clearButtonListener() {
        specialtySelect.clear();
        select.setValue(CourseSearchType.ACTIVE.getDisplay());
        fillCoursesList(true);
    }

    private void searchCoursesEventListener() {
       fillCoursesList(specialtySelect.getValue(), null, null,
               StudyUtils.defineSearchMode(select.getValue()));
    }

    private void selectChangeListener() {
        Boolean searchMode = StudyUtils.defineSearchMode(select.getValue());
        //todo get filters
        fillCoursesList(searchMode);
    }

    private void fillCoursesList(Boolean searchMode) {
        fillCoursesList(null, null, null, searchMode);
    }

    private void fillCoursesList(String specialty, String form, String stage, Boolean searchMode) {

        coursesListLayout.removeAll();
        List<Course> courseList = studyClient.getTeacherCourses(
                profileInfo.getLogin(), specialty, form, stage, searchMode);

        courseList.forEach(course -> {
            coursesListLayout.add(createCourseDisplayItem(course));
        });
    }

    private HorizontalLayout createCourseDisplayItem(Course course) {
        VerticalLayout innerLayout = new VerticalLayout();
        Label title = new Label(course.getDiscipline().getSpecialty() + " " + course.getDiscipline().getTitle());
        Label descr = new Label(getDescription(course));
        descr.getStyle().set("font-size", "var(--lumo-font-size-s)");
        innerLayout.add(title, descr);
        innerLayout.setSpacing(false);

        //element layout
        HorizontalLayout layout = StudyUtils.getCourseItemLayout();

        layout.add(innerLayout, StudyUtils.getExaminationLabel(course));
        layout.expand(innerLayout);

        layout.addClickListener(e -> layout.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailPage.class, course.getId())
        ));

        return layout;
    }

    private String getDescription(Course course) {
        return String.format("%s курс. %s. %s форма",
                //calc course num
                course.getDiscipline().getSemester(),
                course.getDiscipline().getStageName(),
                course.getDiscipline().getStudyFormShortName());
    }
}
