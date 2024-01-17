package com.anast.lms.views;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

public class RegistrationView extends VerticalLayout {

    private final TextField loginField;
    private final EmailField mailField;
    private final TextField nameField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;


    public RegistrationView() {

        loginField = new TextField("Логин");
        loginField.setRequired(true);
        loginField.setWidth("55%");

        mailField = new EmailField("Адрес электроной почты");
        mailField.setErrorMessage("Некорректный адрес");
        mailField.setClearButtonVisible(true);
        mailField.setWidthFull();

        nameField = new TextField("ФИО");
        nameField.setRequired(true);
        nameField.setWidthFull();

        passwordField = new PasswordField("Придумайте пароль");
        passwordField.setRequired(true);
        passwordField.setWidth("55%");

        confirmPasswordField = new PasswordField("Подтвердите пароль");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidth("55%");


        add(nameField, mailField, loginField, passwordField, confirmPasswordField);
        setWidth("35%");
        setSpacing(false);

    }

    public TextField getLoginField() {
        return loginField;
    }

    public EmailField getMailField() {
        return mailField;
    }

    public TextField getNameField() {
        return nameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public PasswordField getConfirmPasswordField() {
        return confirmPasswordField;
    }
}
