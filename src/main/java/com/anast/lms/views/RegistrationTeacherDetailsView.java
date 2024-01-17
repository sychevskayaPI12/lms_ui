package com.anast.lms.views;

import com.anast.lms.model.Department;
import com.anast.lms.model.FacultyPosition;
import com.anast.lms.model.profile.TeacherFacultyPosition;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class RegistrationTeacherDetailsView extends VerticalLayout {

    private final StudyServiceClient studyServiceClient;

    private VerticalLayout itemsContainer;

    public RegistrationTeacherDetailsView(StudyServiceClient studyServiceClient) {
        this.studyServiceClient = studyServiceClient;

        itemsContainer = new VerticalLayout();
        itemsContainer.setSpacing(false);
        buildItem(true);

        Button addButton = new Button("Добавить должность", new Icon(VaadinIcon.PLUS));
        addButton.addClickListener(e -> buildItem(false));

        add(itemsContainer, addButton);
    }

    private void buildItem(boolean isInitial) {
        HorizontalLayout itemLayout = new HorizontalLayout();

        TeacherPositionItemView view = new TeacherPositionItemView();
        Button clearItem = new Button(new Icon(VaadinIcon.CLOSE));
        clearItem.setVisible(!isInitial);
        clearItem.addClickListener(e -> itemsContainer.remove(itemLayout));
        clearItem.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        itemLayout.add(view, clearItem);
        itemLayout.setVerticalComponentAlignment(Alignment.END, view, clearItem);
        itemsContainer.add(itemLayout);
    }

    public List<TeacherFacultyPosition> getTeacherFacultyPositions() throws Exception {
        List<TeacherFacultyPosition> positions = new ArrayList<>();

        List<TeacherPositionItemView> items = itemsContainer.getChildren()
                .map(c-> (TeacherPositionItemView) c.getChildren().findFirst().get())
                .collect(Collectors.toList());

        for(TeacherPositionItemView item : items) {
            if(item.positionSelect.getValue() == null || item.departmentSelect.getValue() == null) {
                throw new Exception("Заполните должность преподавателя");
            }
            TeacherFacultyPosition position = new TeacherFacultyPosition(
              item.positionSelect.getValue(),
              item.departmentSelect.getValue()
            );
            positions.add(position);
        }
        return positions;
    }


    public class TeacherPositionItemView extends HorizontalLayout {

        private Select<Department> departmentSelect;
        private Select<FacultyPosition> positionSelect;

        public TeacherPositionItemView() {

            departmentSelect = new Select<>();
            departmentSelect.setItemLabelGenerator(Department::getFullTitle);
            departmentSelect.setLabel("Кафедра");
            departmentSelect.setItems(studyServiceClient.getDepartments());

            positionSelect = new Select<>();
            positionSelect.setItemLabelGenerator(FacultyPosition::getTitle);
            positionSelect .setLabel("Должность");
            positionSelect.setItems(studyServiceClient.getFacultyPositions());


            add(departmentSelect, positionSelect);
        }

        public Select<Department> getDepartmentSelect() {
            return departmentSelect;
        }

        public Select<FacultyPosition> getPositionSelect() {
            return positionSelect;
        }
    }
}
