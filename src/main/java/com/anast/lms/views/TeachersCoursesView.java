package com.anast.lms.views;

import com.anast.lms.model.CourseSearchType;
import com.anast.lms.model.Discipline;
import com.anast.lms.model.DisciplineInstance;
import com.anast.lms.model.course.Course;
import com.anast.lms.model.profile.UserProfile;
import com.anast.lms.service.StudyUtils;
import com.anast.lms.service.external.CourseServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Route(value = "/my_courses/teacher")
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class TeachersCoursesView extends VerticalLayout {

    private final StudyServiceClient studyClient;
    private final CourseServiceClient courseServiceClient;
    private final UserProfile userProfile;

    private VerticalLayout coursesListLayout;
    private Select<String> select;
    private Select<String> specialtySelect;


    public TeachersCoursesView(StudyServiceClient studyClient, CourseServiceClient courseServiceClient, UserProfile userProfile) {
        this.studyClient = studyClient;
        this.courseServiceClient = courseServiceClient;
        this.userProfile = userProfile;

        setWidthFull();
        setHeightFull();
        build();
    }

    private void build() {

        if(userProfile.getTeacherInfo() == null) {
            return;
        }

        HorizontalLayout barLayout = new HorizontalLayout();

        Button createButton = new Button("Создать курс", new Icon(VaadinIcon.PLUS));
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(e -> createAndShowDialog(userProfile.getUserProfileInfo().getLogin()));

        select = new Select<>();
        select.setItems(Arrays.stream(CourseSearchType.values())
                .map(CourseSearchType::getDisplay).collect(Collectors.toList()));
        select.setValue(CourseSearchType.ACTIVE.getDisplay());
        select.addValueChangeListener(e -> selectChangeListener());

        specialtySelect = new Select<>();
        specialtySelect.setPlaceholder("Направление");
        specialtySelect.setItems(studyClient.getSpecialties());

        Button searchButton = new Button("Найти", new Icon(VaadinIcon.SEARCH));
        searchButton.addClickListener(e -> searchCoursesEventListener());

        Button clearButton = new Button("Сбросить");
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.addClickListener(e -> clearButtonListener());

        barLayout.add(createButton, select, specialtySelect, searchButton, clearButton);

        coursesListLayout = new VerticalLayout();
        coursesListLayout.setPadding(false);

        fillCoursesList(true);

        Scroller scroller = new Scroller(coursesListLayout);
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        scroller.setWidth("80%");
        scroller.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
                .set("padding", "var(--lumo-space-m)");

        add(barLayout, scroller);

    }

    private void clearButtonListener() {
        specialtySelect.clear();
        select.setValue(CourseSearchType.ACTIVE.getDisplay());
        fillCoursesList(true);
    }

    private void searchCoursesEventListener() {
       fillCoursesList(specialtySelect.getValue(), null, null,
               StudyUtils.defineSearchMode(select.getValue()));
    }

    private void selectChangeListener() {
        Boolean searchMode = StudyUtils.defineSearchMode(select.getValue());
        //todo get filters
        fillCoursesList(searchMode);
    }

    private void fillCoursesList(Boolean searchMode) {
        fillCoursesList(null, null, null, searchMode);
    }

    private void fillCoursesList(String specialty, String form, String stage, Boolean searchMode) {

        coursesListLayout.removeAll();
        List<Course> courseList = studyClient.getTeacherCourses(
                userProfile.getUserProfileInfo().getLogin(), specialty, form, stage, searchMode);

        courseList.forEach(course -> {
            coursesListLayout.add(createCourseDisplayItem(course));
        });
    }

    private HorizontalLayout createCourseDisplayItem(Course course) {
        VerticalLayout innerLayout = new VerticalLayout();
        Label title = new Label(course.getDiscipline().getSpecialty() + " " + course.getDiscipline().getTitle());
        Label descr = new Label(getDescription(course));
        descr.getStyle().set("font-size", "var(--lumo-font-size-s)");
        innerLayout.add(title, descr);
        innerLayout.setSpacing(false);

        //element layout
        HorizontalLayout layout = StudyUtils.getCourseItemLayout();

        layout.add(innerLayout, StudyUtils.getExaminationLabel(course));
        layout.expand(innerLayout);

        layout.addClickListener(e -> layout.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailPage.class, course.getId())
        ));

        return layout;
    }

    private String getDescription(Course course) {
        return String.format("%s курс. %s. %s форма",
                //calc course num
                course.getDiscipline().getSemester(),
                course.getDiscipline().getStageName(),
                course.getDiscipline().getStudyFormShortName());
    }

    private void createAndShowDialog(String login) {

        List<DisciplineInstance> disciplineInstances = studyClient.getTeacherDisciplines(login);
        Dialog dialog = new Dialog();
        dialog.setWidth("50%");
        VerticalLayout dialogLayout = createDialogLayout(dialog, disciplineInstances);
        dialog.add(dialogLayout);

        dialog.open();
    }

    private VerticalLayout createDialogLayout(Dialog dialog, List<DisciplineInstance> disciplineInstances) {
        H2 headline = new H2("Добавить новый курс");
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        Select<DisciplineInstance> disciplineInstanceSelect = new Select<>();
        disciplineInstanceSelect.setLabel("Дисциплина");
        disciplineInstanceSelect.setItems(disciplineInstances);
        //todo формировать более полную строку
        disciplineInstanceSelect.setItemLabelGenerator(Discipline::getTitle);

        Button confirmButton = new Button("Создать");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.setEnabled(false);
        confirmButton.addClickListener(c-> {
            if (disciplineInstanceSelect.getValue() != null) {
                courseServiceClient.createNewCourse(disciplineInstanceSelect.getValue().getId());
                fillCoursesList(true);
                dialog.close();
            }

        });
        disciplineInstanceSelect.addValueChangeListener(e -> confirmButton.setEnabled(true));

        Button cancelButton = new Button("Отмена", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(confirmButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(headline, disciplineInstanceSelect, buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.setWidthFull();

        return dialogLayout;

    }
}
