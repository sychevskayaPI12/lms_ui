package com.anast.lms.views;

import com.anast.lms.model.*;
import com.anast.lms.model.profile.StudentProfileInfo;
import com.anast.lms.model.profile.TeacherProfileInfo;
import com.anast.lms.model.profile.UserProfile;
import com.anast.lms.model.profile.UserProfileInfo;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.external.UserServiceClient;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
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
import java.util.stream.Collectors;

@Route("registration")
@PageTitle("Registration | LMS")
public class RegistrationPage extends VerticalLayout {

    private final UserServiceClient userServiceClient;
    private final StudyServiceClient studyServiceClient;
    private final BCryptPasswordEncoder passwordEncoder;

    private RegistrationView registrationView;
    private RegistrationDetailsView registrationDetailsView;

    public RegistrationPage(UserServiceClient userServiceClient, StudyServiceClient studyServiceClient,
                            BCryptPasswordEncoder passwordEncoder) {
        this.userServiceClient = userServiceClient;
        this.studyServiceClient = studyServiceClient;
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

        if(!isRequiredFieldsFilled(registrationView)) {
            Notification notification = new Notification("Заполните обязательные поля");
            notification.setOpened(true);
            return;
        }

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
        if(roles.isEmpty()) {
            Notification notification = new Notification("Пожалуйста, укажите свои роли");
            notification.setOpened(true);
            return;
        }

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
            UserProfile profileInfo = fillUserProfileInfo();
            studyServiceClient.saveProfileInfo(profileInfo);

        } catch (Exception e) {
            Notification notification = new Notification("Ошибка регистрации: " + e.getMessage());
            notification.setOpened(true);

            //rollback user
            userServiceClient.deleteUser(userDetail.getLogin());
            return;
        }

        //todo желательно конечно поместить юзера в контекст и перейти на стартовую

        Notification notification = new Notification(String.format("Регистрация пользователя с логином %s прошла успешно! Пожалуйста, авторизируйтесь."
                , userDetail.getLogin()));
        notification.setOpened(true);

        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }

    private boolean isRequiredFieldsFilled(RegistrationView registrationView) {

        List<Component> fields = registrationView.getChildren()
                .filter(c -> ((AbstractField) c).isEmpty()).collect(Collectors.toList());
        return fields.isEmpty();
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

    private UserProfile fillUserProfileInfo() throws Exception {

        UserProfile userProfile = new UserProfile();
        UserProfileInfo userProfileInfo = new UserProfileInfo();
        userProfileInfo.setLogin(registrationView.getLoginField().getValue());
        userProfile.setUserProfileInfo(userProfileInfo);

        boolean isTeacherFlag = registrationDetailsView.isTeacher();
        boolean isStudentFlag = registrationDetailsView.isStudent();

        if(isTeacherFlag) {
            //todo
            TeacherProfileInfo teacherProfileInfo = new TeacherProfileInfo();
            userProfile.setTeacherInfo(teacherProfileInfo);
        }

        if(isStudentFlag) {

            if(registrationDetailsView.getGroupSelect().getValue() == null) {
                throw new Exception("Не указана группа студента");
            }
            StudentProfileInfo studentProfileInfo = new StudentProfileInfo();
            studentProfileInfo.setGroupCode(registrationDetailsView.getGroupSelect().getValue());
            userProfile.setStudentInfo(studentProfileInfo);
        }

        return userProfile;

    }
}
