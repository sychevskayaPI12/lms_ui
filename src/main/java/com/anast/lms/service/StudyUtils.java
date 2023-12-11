package com.anast.lms.service;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class StudyUtils {

    public static Boolean defineSearchMode(String selectValue) {

        CourseSearchType type = CourseSearchType.getEnum(selectValue);
        switch (type) {
            case ACTIVE: return true;
            case DONE: return false;
            default: return null;
        }
    }

    public static HorizontalLayout getCourseItemLayout() {
        HorizontalLayout layout = new HorizontalLayout();

        layout.getStyle().set("background-color", "ghostwhite")
                .set("border" , "1px solid lavender")
                .set("border-radius", "var(--lumo-border-radius-s)");
        layout.setWidth("95%");
        layout.setPadding(false);
        layout.setVerticalComponentAlignment(FlexComponent.Alignment.START);
        return layout;
    }

    public static Label getExaminationLabel(Course course) {
        Label label = new Label();

        if(course.getDiscipline().isExamination()) {
            label.setText("Экзамен");
            label.getStyle()
                    .set("border-top", "1px solid coral")
                    //.set("border-bottom", "1px solid coral")
                    .set("color", "coral")
                    .set("border-radius", "var(--lumo-border-radius-s)");

        } else {
            label.setText("Зачет");
            label.getStyle()
                    .set("border-top", "1px solid cadetblue")
                    //.set("border-bottom", "1px solid cadetblue")
                    .set("color", "cadetblue")
                    .set("border-radius", "var(--lumo-border-radius-s)");
        }
        return label;
    }
}
