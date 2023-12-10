package com.anast.lms.views;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

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

        //todo filters fields + button
        filterLayout.add(select);

        coursesListLayout = new VerticalLayout();
        coursesListLayout.setPadding(false);

        //todo add scroll

        fillCoursesList(true);

        add(select, coursesListLayout);

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
        //todo
        return new HorizontalLayout();
    }
}
