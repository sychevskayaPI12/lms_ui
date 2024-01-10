package com.anast.lms.views;

import com.anast.lms.model.*;
import com.anast.lms.service.external.ProfileServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.external.UserServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Route("registration")
@PageTitle("Registration | LMS")
public class RegistrationPage extends VerticalLayout {

    private final UserServiceClient userServiceClient;
    private final StudyServiceClient studyServiceClient;
    private final ProfileServiceClient profileServiceClient;
    private final BCryptPasswordEncoder passwordEncoder;

    private RegistrationView registrationView;
    private RegistrationDetailsView registrationDetailsView;

    public RegistrationPage(UserServiceClient userServiceClient, StudyServiceClient studyServiceClient, ProfileServiceClient profileServiceClient, BCryptPasswordEncoder passwordEncoder) {
        this.userServiceClient = userServiceClient;
        this.studyServiceClient = studyServiceClient;
        this.profileServiceClient = profileServiceClient;
        this.passwordEncoder = passwordEncoder;

        //setSpacing(false);
        build();
    }

    private void build() {

        H1 header = new H1("Регистрация");
        header.setWidth("70%");

        registrationView = new RegistrationView();
        registrationDetailsView = new RegistrationDetailsView(studyServiceClient);

        HorizontalLayout contentHorizontalLayout = new HorizontalLayout(registrationView, registrationDetailsView);
        contentHorizontalLayout.setWidth("70%");

        Button save = new Button("Сохранить");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> saveButtonEventListener());

        Button cancel = new Button("Отмена");
        cancel.addClickListener(e -> {
            cancel.getUI().ifPresent(ui -> ui.navigate(LoginView.class));
        });

        HorizontalLayout panel = new HorizontalLayout(save, cancel);
        panel.setWidth("70%");
        panel.setJustifyContentMode(JustifyContentMode.END);

        add(header, contentHorizontalLayout, panel);
        setHorizontalComponentAlignment(Alignment.CENTER, header, contentHorizontalLayout, panel);
    }

    private void saveButtonEventListener() {
        PasswordField passwordField = registrationView.getPasswordField();
        PasswordField confirmPasswordField = registrationView.getConfirmPasswordField();

        if(!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            Notification notification = new Notification("Значения пароля не совпадают");
            notification.setOpened(true);
            return;
        }

        UserDetail userDetail = new UserDetail(
                registrationView.getLoginField().getValue(),
                registrationView.getNameField().getValue(),
                registrationView.getMailField().getValue());

        String passwordEncoded = passwordEncoder.encode(passwordField.getValue());
        List<String> roles = defineUserRoles();

        UserAuthInfo userAuthInfo = new UserAuthInfo(
                registrationView.getLoginField().getValue(),
                passwordEncoded, roles);

        UserRegisterRequest registerRequest = new UserRegisterRequest(userAuthInfo, userDetail);

        try {
            userServiceClient.registerNewUser(registerRequest);
        } catch (Exception e) {
            e.printStackTrace();

            Notification notification = new Notification("Ошибка регистрации: " + e.getMessage());
            notification.setOpened(true);
            return;
        }

        try {
            UserProfileInfo profileInfo = fillUserProfileInfo();
            profileServiceClient.saveProfileInfo(profileInfo);

        } catch (Exception e) {
            Notification notification = new Notification("Ошибка регистрации: " + e.getMessage());
            notification.setOpened(true);

            //rollback user
            userServiceClient.deleteUser(userDetail.getLogin());
            return;
        }

        //todo желательно конечно поместить юзера в контекст и перейти на стартовую
        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }

    private List<String> defineUserRoles() {
        List<String> roles = new ArrayList<>();
        if(registrationDetailsView.isTeacher()) {
            roles.add("TEACHER");
        }
        if(registrationDetailsView.isStudent()) {
            roles.add("STUDENT");
        }
        return roles;
    }

    private UserProfileInfo fillUserProfileInfo() {
        UserProfileInfo profileInfo = new UserProfileInfo();
        profileInfo.setLogin(registrationView.getLoginField().getValue());

        boolean isTeacherFlag = registrationDetailsView.isTeacher();
        boolean isStudentFlag = registrationDetailsView.isStudent();

        if(isTeacherFlag) {
            //todo
            TeacherProfileInfo teacherProfileInfo = new TeacherProfileInfo();
            profileInfo.setTeacherInfo(teacherProfileInfo);
        }

        if(isStudentFlag) {
            StudentProfileInfo studentProfileInfo = new StudentProfileInfo();
            studentProfileInfo.setGroupCode(registrationDetailsView.getGroupSelect().getValue());
            profileInfo.setStudentInfo(studentProfileInfo);
        }

        return profileInfo;

    }
}
