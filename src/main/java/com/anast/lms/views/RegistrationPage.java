package com.anast.lms.views;

import com.anast.lms.model.UserAuthInfo;
import com.anast.lms.model.UserDetail;
import com.anast.lms.model.UserRegisterRequest;
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

@Route("registration")
@PageTitle("Registration | LMS")
public class RegistrationPage extends VerticalLayout {

    private final UserServiceClient userServiceClient;
    private final BCryptPasswordEncoder passwordEncoder;

    private RegistrationView registrationView;
    private RegistrationDetailsView registrationDetailsView;

    public RegistrationPage(UserServiceClient userServiceClient, BCryptPasswordEncoder passwordEncoder) {
        this.userServiceClient = userServiceClient;
        this.passwordEncoder = passwordEncoder;

        //setSpacing(false);
        build();
    }

    private void build() {

        H1 header = new H1("Регистрация");
        header.setWidth("70%");

        registrationView = new RegistrationView();
        registrationDetailsView = new RegistrationDetailsView();

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

        //todo в какой момент заполняем роли?
        String passwordEncoded = passwordEncoder.encode(passwordField.getValue());

        UserAuthInfo userAuthInfo = new UserAuthInfo(
                registrationView.getLoginField().getValue(),
                passwordEncoded, new ArrayList<>());

        UserRegisterRequest registerRequest = new UserRegisterRequest(userAuthInfo, userDetail);

        try {
            userServiceClient.registerNewUser(registerRequest);
        } catch (Exception e) {
            e.printStackTrace();

            Notification notification = new Notification("Ошибка регистрации: " + e.getMessage());
            notification.setOpened(true);
            return;
        }

        //todo желательно конечно поместить юзера в контекст и перейти на стартовую
        getUI().ifPresent(ui -> ui.navigate(LoginView.class));
    }
}
