package com.anast.lms.service;

import com.anast.lms.model.Course;
import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.SchedulerItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.List;

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

    public static Grid<SchedulerItem> getStudentDailyGrid(List<SchedulerItem> items) {
        Grid<SchedulerItem> grid = new Grid<>(SchedulerItem.class, false);
        grid.addColumn(SchedulerItem::getNumber).setHeader("Пара").setAutoWidth(true);
        grid.addColumn(item -> item.getDiscipline().getTitle()).setHeader("Дисциплина")
                .setAutoWidth(true);
        grid.addColumn(item -> item.getClassType().getTitle()).setAutoWidth(true);
        grid.addColumn(SchedulerItem::getClassRoom).setHeader("Аудитория").setAutoWidth(true);


        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setAllRowsVisible(true);
        grid.setItems(items);
        return grid;
    }

    public static Grid<SchedulerItem> getTeacherDailyGrid(List<SchedulerItem> items) {
        Grid<SchedulerItem> grid = new Grid<>(SchedulerItem.class, false);
        grid.addColumn(SchedulerItem::getNumber).setHeader("Пара").setAutoWidth(true);
        grid.addColumn(item -> item.getDiscipline().getTitle()).setHeader("Дисциплина")
                .setAutoWidth(true);
        grid.addColumn(item -> item.getClassType().getTitle()).setAutoWidth(true);
        grid.addColumn(SchedulerItem::getGroups).setHeader("Группы").setAutoWidth(true);
        grid.addColumn(SchedulerItem::getClassRoom).setHeader("Аудитория").setAutoWidth(true);


        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.setAllRowsVisible(true);
        grid.setItems(items);
        return grid;
    }
}
