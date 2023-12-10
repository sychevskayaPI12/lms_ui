package com.anast.lms.views;

import com.anast.lms.model.UserProfileInfo;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
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
            //todo избавиться от textarea..
            if(profileInfo.getStudentInfo() != null) {
                TextArea studentInfoDiv = new TextArea();
                studentInfoDiv.setValue(String.format("Студент %s курса\nГруппа %s",
                        profileInfo.getStudentInfo().getCourse(),
                        profileInfo.getStudentInfo().getGroupCode()));
                studentInfoDiv.setReadOnly(true);
                studentInfoDiv.getStyle()
                        .set("background-color", "lavender")
                       // .set("border" , "1px solid palegoldenrod")
                        .set("border-radius", "var(--lumo-border-radius-s)");

                infoLayout.add(studentInfoDiv);
            }
            if(profileInfo.getTeacherInfo() != null) {
                TextArea teacherInfoDiv = new TextArea();
                teacherInfoDiv.setValue(profileInfo.getTeacherInfo().getDegree());
                teacherInfoDiv.setReadOnly(true);
                teacherInfoDiv.getStyle()
                        .set("background-color", "floralwhite")
                        // .set("border" , "1px solid palegoldenrod")
                        .set("border-radius", "var(--lumo-border-radius-s)");
                infoLayout.add(teacherInfoDiv);
            }
        }
        add(infoLayout);
    }
}
