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

        setPadding(true);
        setSpacing(true);
        Label header = new Label();
        header.setWidthFull();
        header.setHeight("3%");
        header.setText(String.format("Добро пожаловать, %s!", profileInfo.getFullName()));
        header.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(header);

        addInfoLayout(profileInfo);

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
}
