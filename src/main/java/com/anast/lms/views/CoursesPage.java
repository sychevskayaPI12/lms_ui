package com.anast.lms.views;

import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "/my_courses", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CoursesPage extends VerticalLayout {

    private final ProfileServiceClient profileClient;
    private final SecurityService securityService;
    private final StudyServiceClient studyClient;

    public CoursesPage(ProfileServiceClient profileClient, SecurityService securityService, StudyServiceClient studyClient) {
        this.profileClient = profileClient;
        this.securityService = securityService;
        this.studyClient = studyClient;

        String login = securityService.getAuthenticatedUser().getUsername();
        UserProfileInfo profileInfo = profileClient.getUserProfileInfo(login);

        //todo debug
        studyClient.getStudentCourser(profileInfo.getStudentInfo().getGroupCode(), true);
        studyClient.getStudentCourser(profileInfo.getStudentInfo().getGroupCode(), false);

        studyClient.getStudentCourser(profileInfo.getStudentInfo().getGroupCode(), null);

    }
}
