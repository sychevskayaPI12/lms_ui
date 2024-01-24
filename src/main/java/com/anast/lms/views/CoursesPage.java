package com.anast.lms.views;

import com.anast.lms.model.profile.*;
import com.anast.lms.service.external.CourseServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.vaadin.tabs.PagedTabs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "/my_courses", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CoursesPage extends VerticalLayout {

    private StudentCoursesView studentCoursesView;
    private TeachersCoursesView teachersCoursesView;
    private CourseServiceClient courseServiceClient;


    public CoursesPage(SecurityService securityService, StudyServiceClient studyClient, CourseServiceClient courseServiceClient) {
        this.courseServiceClient = courseServiceClient;

        UserDetails userDetails = securityService.getAuthenticatedUser();
        UserProfile profileInfo = studyClient.getUserProfileInfo(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        this.studentCoursesView = new StudentCoursesView(studyClient, profileInfo);
        this.teachersCoursesView = new TeachersCoursesView(studyClient, this.courseServiceClient, profileInfo);

        //отображение компонентов
        buildCoursesViewsByUserRoles(roles);
        setSpacing(false);

    }

    private void buildCoursesViewsByUserRoles(List<String> roles) {
        if(roles.containsAll(Arrays.asList("ROLE_TEACHER", "ROLE_STUDENT"))) {

            VerticalLayout container = new VerticalLayout();
            PagedTabs tabs = new PagedTabs(container);
            tabs.add("Курсы студента", this.studentCoursesView, false);
            tabs.add("Курсы преподавателя", this.teachersCoursesView, false);
            add(tabs, container);

        } else if(roles.contains("ROLE_TEACHER")) {
            add(this.teachersCoursesView);

        } else if(roles.contains("ROLE_STUDENT")) {
            add(this.studentCoursesView);
        }
    }
}
