package com.anast.lms.views;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

public class RegistrationDetailsView extends VerticalLayout {

    private Checkbox isStudentCheckBox;
    private Checkbox isTeacherCheckBox;
    private VerticalLayout teacherLayout;
    private VerticalLayout studentLayout;



    public RegistrationDetailsView() {

        isTeacherCheckBox = new Checkbox("Я преподаватель");
        isTeacherCheckBox.addValueChangeListener(e -> teacherLayout.setVisible(isTeacherCheckBox.getValue()));
        buildTeacherLayout();

        isStudentCheckBox = new Checkbox("Я студент");
        isStudentCheckBox.addValueChangeListener(e -> studentLayout.setVisible(isStudentCheckBox.getValue()));
        buildStudentLayout();


        add(isTeacherCheckBox, teacherLayout, isStudentCheckBox, studentLayout);
        setMaxWidth("35%");
        setSpacing(false);
    }

    private void buildTeacherLayout() {
        teacherLayout = new VerticalLayout();
        teacherLayout.setVisible(false);
        //todo выбор должности. Может их несколько должно быть?
    }

    private void buildStudentLayout() {

        Select<String> specialtySelect = new Select<>();
        specialtySelect.setLabel("Направление подготовки");
        specialtySelect.setRequiredIndicatorVisible(true);

        Select<String> stageSelect = new Select<>();
        stageSelect.setLabel("Степень подготовки");
        stageSelect.setRequiredIndicatorVisible(true);

        Select<String> studyFormSelect = new Select<>();
        studyFormSelect.setLabel("Форма обучения");
        studyFormSelect.setRequiredIndicatorVisible(true);

        Select<Integer> courseNumberSelect = new Select<>();
        courseNumberSelect.setLabel("Курс");
        courseNumberSelect.setRequiredIndicatorVisible(true);

        Select<String> groupSelect = new Select<>();
        groupSelect.setLabel("Группа");
        groupSelect.setRequiredIndicatorVisible(true);

        HorizontalLayout row1 = new HorizontalLayout(specialtySelect, stageSelect, studyFormSelect);
        HorizontalLayout row2 = new HorizontalLayout(courseNumberSelect, groupSelect);

        studentLayout = new VerticalLayout(row1, row2);
        studentLayout.setVisible(false);

    }

    public Checkbox getIsStudentCheckBox() {
        return isStudentCheckBox;
    }

    public Checkbox getIsTeacherCheckBox() {
        return isTeacherCheckBox;
    }

    public VerticalLayout getTeacherLayout() {
        return teacherLayout;
    }

    public VerticalLayout getStudentLayout() {
        return studentLayout;
    }

    private void fillSpecialties() {

    }
}
