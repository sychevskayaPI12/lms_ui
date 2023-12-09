package com.anast.lms.views;

import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(layout=MainLayout.class)
@PageTitle("Главная | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {

    private final ProfileServiceClient profileClient;
    private final SecurityService securityService;


    public MainView(ProfileServiceClient profileClient, SecurityService securityService) {
        this.profileClient = profileClient;
        this.securityService = securityService;
        build();
    }

    private void build() {

        String login = securityService.getAuthenticatedUser().getUsername();
        UserProfileInfo profileInfo = profileClient.getUserProfileInfo(login);

        H2 header = new H2();
        header.setWidthFull();
        header.setHeight("3%");
        header.setText(String.format("Добро пожаловать, %s!", profileInfo.getFullName()));
        header.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(header);

        HorizontalLayout infoLayout = new HorizontalLayout();
        if(profileInfo.getTeacherInfo() == null && profileInfo.getStudentInfo() == null) {
            //todo
            infoLayout.add(new Button("Добавить информацию"));
        } else {
            if(profileInfo.getTeacherInfo() != null) {
                TextArea teacherInfoDiv = new TextArea();
                teacherInfoDiv.setValue(profileInfo.getTeacherInfo().getDegree());
                teacherInfoDiv.setReadOnly(true);
                infoLayout.add(teacherInfoDiv);
            }
            if(profileInfo.getStudentInfo() != null) {
                TextArea studentInfoDiv = new TextArea();
                studentInfoDiv.setValue(String.format("Студент %s курса\nГруппа %s",
                        profileInfo.getStudentInfo().getCourse(),
                        profileInfo.getStudentInfo().getGroupCode()));
                studentInfoDiv.setReadOnly(true);
                infoLayout.add(studentInfoDiv);
            }
        }
        add(infoLayout);
    }
}
