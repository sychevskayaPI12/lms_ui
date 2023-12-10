package com.anast.lms.views;

import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


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
        add(new Label("Тут будут курсы преподавателя"));
    }
}
