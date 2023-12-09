package com.anast.lms.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Login | LMS")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView(){
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setI18n(createLoginI18n());
        login.setAction("login");

        add(login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }

    private LoginI18n createLoginI18n(){
        LoginI18n i18n = LoginI18n.createDefault();

        i18n.getForm().setUsername("Логин");
        i18n.getForm().setTitle("Вход в ситему:");
        i18n.getForm().setSubmit("Войти");
        i18n.getForm().setPassword("Пароль");
        i18n.getForm().setForgotPassword("Забыли пароль?");
       /* i18n.getErrorMessage().setTitle("Usuário/senha inválidos");
        i18n.getErrorMessage()
                .setMessage("Confira seu usuário e senha e tente novamente.");
        i18n.setAdditionalInformation(
                "Caso necessite apresentar alguma informação extra para o usuário"
                        + " (como credenciais padrão), este é o lugar.");*/
        return i18n;
    }
}

