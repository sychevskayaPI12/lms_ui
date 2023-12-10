package com.anast.lms.views;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

        //todo add scroll

        fillCoursesList(true);

        add(select, coursesListLayout);

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
        Label teachers = new Label(getTeachersText(course.getTeachers()));
        innerLayout.add(title, teachers);
        innerLayout.setSpacing(false);

        //element layout
        HorizontalLayout layout = new HorizontalLayout();

        layout.getStyle().set("background-color", "ghostwhite")
                .set("border" , "1px solid lavender")
                .set("border-radius", "var(--lumo-border-radius-s)");
        layout.setWidth("70%");
        layout.setPadding(true);
        layout.setVerticalComponentAlignment(Alignment.START);

        layout.add(innerLayout, getExaminationLabel(course));
        layout.expand(innerLayout);
        return layout;
    }

    private Label getExaminationLabel(Course course) {
        Label label = new Label();

        if(course.getDiscipline().isExamination()) {
            label.setText("Экзамен");
            label.getStyle()
                    .set("border-top", "1px solid coral")
                    //.set("border-bottom", "1px solid coral")
                    .set("color", "coral")
                    .set("border-radius", "var(--lumo-border-radius-s)");

        } else {
            label.setText("Зачет");
            label.getStyle()
                    .set("border-top", "1px solid cadetblue")
                    //.set("border-bottom", "1px solid cadetblue")
                    .set("color", "cadetblue")
                    .set("border-radius", "var(--lumo-border-radius-s)");
        }
        return label;
    }

    private String getTeachersText(Map<String, String> names) {
        if(names.size() == 0) {
            return "Преподаватель еще не назначен";
        }
        if(names.size() == 1) {
            return "Преподаватель: " + names.values().stream().findFirst().get();
        }

        return "Преподаватели: " + String.join(", ", names.values());
    }
}
