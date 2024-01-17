package com.anast.lms.views;

import com.anast.lms.model.Stage;
import com.anast.lms.model.StudyForm;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.ArrayList;
import java.util.List;

public class RegistrationDetailsView extends VerticalLayout {

    private final StudyServiceClient studyServiceClient;

    private Checkbox isStudentCheckBox;
    private Checkbox isTeacherCheckBox;
    private RegistrationTeacherDetailsView teacherLayout;
    private VerticalLayout studentLayout;


    private Select<String> specialtySelect;
    private Select<Stage> stageSelect;
    private Select<StudyForm> studyFormSelect;
    private Select<Integer> courseNumberSelect;
    private Select<String> groupSelect;


    public RegistrationDetailsView(StudyServiceClient studyServiceClient) {

        this.studyServiceClient = studyServiceClient;
        setSpacing(false);

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
        teacherLayout = new RegistrationTeacherDetailsView(studyServiceClient);
        teacherLayout.setVisible(false);
    }

    public boolean isStudent() {
        return getIsStudentCheckBox().getValue() != null
                && getIsStudentCheckBox().getValue();
    }

    public boolean isTeacher() {
        return getIsTeacherCheckBox().getValue() != null
                && getIsTeacherCheckBox().getValue();
    }

    private void buildStudentLayout() {

        specialtySelect = new Select<>();
        specialtySelect.setLabel("Направление подготовки");
        specialtySelect.setRequiredIndicatorVisible(true);
        fillSpecialties();
        specialtySelect.addValueChangeListener(e -> selectValueChangeListenerForGroup());

        stageSelect = new Select<>();
        stageSelect.setLabel("Степень подготовки");
        stageSelect.setRequiredIndicatorVisible(true);
        fillStages();
        stageSelect.addValueChangeListener(e -> selectValueChangeListenerForCourseNum());
        stageSelect.addValueChangeListener(e -> selectValueChangeListenerForGroup());

        studyFormSelect = new Select<>();
        studyFormSelect.setLabel("Форма обучения");
        studyFormSelect.setRequiredIndicatorVisible(true);
        fillStudyForms();
        studyFormSelect.addValueChangeListener(e -> selectValueChangeListenerForCourseNum());
        studyFormSelect.addValueChangeListener(e -> selectValueChangeListenerForGroup());

        courseNumberSelect = new Select<>();
        courseNumberSelect.setLabel("Курс");
        courseNumberSelect.setRequiredIndicatorVisible(true);
        courseNumberSelect.setEnabled(false);
        courseNumberSelect.addValueChangeListener(e -> selectValueChangeListenerForGroup());

        groupSelect = new Select<>();
        groupSelect.setLabel("Группа");
        groupSelect.setRequiredIndicatorVisible(true);
        groupSelect.setEnabled(false);


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

    public RegistrationTeacherDetailsView getTeacherLayout() {
        return teacherLayout;
    }

    public VerticalLayout getStudentLayout() {
        return studentLayout;
    }

    public Select<String> getGroupSelect() {
        return groupSelect;
    }

    private void fillSpecialties() {
        specialtySelect.setItems(studyServiceClient.getSpecialties());
    }

    private void fillStages() {
        stageSelect.setItems(studyServiceClient.getStages());
        stageSelect.setItemLabelGenerator(Stage::getTitle);
    }

    private void fillStudyForms() {
        studyFormSelect.setItems(studyServiceClient.getStudyForms());
        studyFormSelect.setItemLabelGenerator(StudyForm::getTitle);
    }

    private void selectValueChangeListenerForCourseNum() {
        if(stageSelect.getValue() != null && studyFormSelect.getValue() != null) {
            courseNumberSelect.setEnabled(true);
            courseNumberSelect.setItems(
                    defineAvailableCourses(
                            stageSelect.getValue().getCode(),
                            studyFormSelect.getValue().getCode())
            );
        } else {
            courseNumberSelect.setEnabled(false);
            courseNumberSelect.clear();
        }
    }

    private void selectValueChangeListenerForGroup() {

        if(specialtySelect.getValue() != null && studyFormSelect.getValue() != null &&
                studyFormSelect.getValue() != null && courseNumberSelect.getValue() != null) {

            String specialty = specialtySelect.getValue();
            String stage = stageSelect.getValue().getCode();
            String studyForm = studyFormSelect.getValue().getCode();
            Integer currentCourseNum = courseNumberSelect.getValue();

            List<String> groups = studyServiceClient.getGroups(specialty, stage, studyForm, currentCourseNum);
            groupSelect.setItems(groups);
            groupSelect.setEnabled(true);
        } else {
            groupSelect.clear();
        }
    }

    private List<Integer> defineAvailableCourses(String stageCode, String studyFormCode) {

        Integer maxCourseNum = null;
        switch (stageCode) {
            case "bac" : {
                maxCourseNum = 4;
                break;
            }
            case "mag" : {
                maxCourseNum = 2;
                break;
            }
            case "spec" : {
                maxCourseNum = 5;
                break;
            }
            case "postgrad" : {
                maxCourseNum = 3;
                break;
            }
            default: return new ArrayList<>();
        }

        if(studyFormCode.equals("distant")) {
            maxCourseNum+=1;
        }
        List<Integer> res = new ArrayList<>();
        for(int i = 1; i <= maxCourseNum; i++) {
            res.add(i);
        }
        return res;
    }
}
