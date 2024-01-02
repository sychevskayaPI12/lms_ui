package com.anast.lms.views;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "/my_courses/student")
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class StudentCoursesView extends VerticalLayout {

    private final StudyServiceClient studyClient;
    private final UserProfileInfo profileInfo;

    private VerticalLayout coursesListLayout;
    private Select<String> select;


    public StudentCoursesView(StudyServiceClient studyClient, UserProfileInfo profileInfo) {
        this.studyClient = studyClient;
        this.profileInfo = profileInfo;

        setWidthFull();
        setHeightFull();
        build();
    }


    private void build() {

        if(profileInfo.getStudentInfo() == null) {
           return;
        }

        select = new Select<>();
        select.setItems(Arrays.stream(CourseSearchType.values())
                .map(CourseSearchType::getDisplay).collect(Collectors.toList()));
        select.setValue(CourseSearchType.ACTIVE.getDisplay());
        select.addValueChangeListener(e -> selectChangeListener());

        coursesListLayout = new VerticalLayout();
        coursesListLayout.setPadding(false);
        fillCoursesList(true);

        Scroller scroller = new Scroller(coursesListLayout);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidth("80%");
        scroller.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "var(--lumo-space-m)");

        add(select, scroller);

    }

    private void selectChangeListener() {
        Boolean searchMode = StudyUtils.defineSearchMode(select.getValue());
        fillCoursesList(searchMode);
    }

    private void fillCoursesList(Boolean searchMode) {

        coursesListLayout.removeAll();
        List<Course> courseList = studyClient.getStudentCourses(
                profileInfo.getStudentInfo().getGroupCode(), searchMode);

        courseList.forEach(course -> {
            coursesListLayout.add(createCourseDisplayItem(course));
        });
    }

    private HorizontalLayout createCourseDisplayItem(Course course) {

        VerticalLayout innerLayout = new VerticalLayout();
        Label title = new Label(course.getDiscipline().getTitle());
        Label teachers = new Label(StudyUtils.getTeachersText(course.getDiscipline().getTeachers()));
        teachers.getStyle().set("font-size", "var(--lumo-font-size-s)");
        innerLayout.add(title, teachers);
        innerLayout.setSpacing(false);

        //element layout
        HorizontalLayout layout = StudyUtils.getCourseItemLayout();

        layout.addClickListener(e -> layout.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailPage.class, course.getId())
        ));
        layout.add(innerLayout, StudyUtils.getExaminationLabel(course));
        layout.expand(innerLayout);
        return layout;
    }

}
