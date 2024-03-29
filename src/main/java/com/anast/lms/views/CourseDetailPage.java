package com.anast.lms.views;

import com.anast.lms.model.DisciplineInstance;
import com.anast.lms.model.course.*;
import com.anast.lms.service.external.CourseServiceClient;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.*;
import java.util.List;

@Route(value = "/my_courses/:id?/view", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CourseDetailPage extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final StudyServiceClient studyClient;
    private final SecurityService securityService;
    private final CourseServiceClient courseClient;

    private Integer currentCourseId;

    public CourseDetailPage(StudyServiceClient studyClient, SecurityService securityService, CourseServiceClient courseClient) {
        this.studyClient = studyClient;

        this.securityService = securityService;
        this.courseClient = courseClient;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        build();
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Integer integer) {
        currentCourseId = integer;

    }

    private void build() {

        CourseFullInfoResponse courseDetailPage = courseClient.getCourseFullInfo(currentCourseId);
        Course course = courseDetailPage.getCourse();
        DisciplineInstance discipline = course.getDiscipline();

        //если логин есть среди преподавателей или пользователь имеет права администратора
        UserDetails userDetails = securityService.getAuthenticatedUser();
        if (discipline.getTeacherLogins().contains(userDetails.getUsername())) {
            buildPanel();
        }

        Label title = new Label(discipline.getTitle());
        title.getStyle().set("font-size", "var(--lumo-font-size-l)")
                        .set("font-weight", "bold");

        Label description = new Label(discipline.getSpecialty() + " " +
                discipline.getStudyFormShortName() + " форма. " + discipline.getStageName());
        add(title, description);

        if(discipline.getDescription() != null && !discipline.getDescription().isEmpty()) {
            Label discDescription = new Label(discipline.getDescription());
            add(discDescription);
        }

        List<CourseModule> modules = courseDetailPage.getModules();
        modules.forEach(module -> add(buildModuleItem(module)));
    }

    private void buildPanel() {
        HorizontalLayout panel = new HorizontalLayout();
        Button editButton = new Button("Редактировать");
        editButton.addClickListener(e -> editButton.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailEditPage.class, currentCourseId)
        ));

        panel.add(editButton);
        add(panel);
    }

    private Details buildModuleItem(CourseModule module) {

        VerticalLayout layout = new VerticalLayout();
        TextArea content = new TextArea();
        content.setValue(module.getContent());
        content.getStyle().set("font-size", "var(--lumo-font-size-s)");
        content.setReadOnly(true);
        content.setWidthFull();
        content.setMaxHeight("400px");
        layout.add(content);

        //материалы модуля
        if(!module.getResources().isEmpty()) {
            VerticalLayout moduleMaterials = new VerticalLayout();
            moduleMaterials.add(new Label("Материалы:"));
            appendResources(moduleMaterials, module.getResources());
            layout.add(moduleMaterials);
        }

        if(!module.getModuleTasks().isEmpty()) {
            VerticalLayout moduleTasks = new VerticalLayout();
            moduleTasks.setSpacing(false);
            moduleTasks.add(new Label("Задания:"));
            appendTasks(moduleTasks, module.getModuleTasks());
            layout.add(moduleTasks);
        }

        layout.setWidthFull();

        Details details = new Details(module.getTitle(), layout);
        details.setOpened(true);
        details.getStyle().set("width", "70%")
                .set("border-top", "1px solid cadetblue")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("font-weight", "bold")
                .set("background-color", "whitesmoke");



        return details;
    }

    private void appendResources(VerticalLayout layout, List<ModuleResource> resources) {

        for (ModuleResource resource : resources) {

            StreamResource streamResource = new StreamResource(resource.getDisplayFileName(), (InputStreamFactory) () -> {
                byte[] fileDataArray = courseClient.getFileData(resource).getBody();
                return new ByteArrayInputStream(fileDataArray);
            });

            Anchor anchor = new Anchor(streamResource, resource.getDisplayFileName());
            anchor.setTarget( "_blank" );  // Specify `_blank` to open in a new browser tab/window.
            layout.add(anchor);
        }
    }

    private void appendTasks(VerticalLayout layout, List<Task> tasks) {

        for(Task task : tasks) {
            VerticalLayout taskLayout = new VerticalLayout();
            taskLayout.add(new Label(task.getTaskTypeEnum().getTitle() + ": " + task.getTitle()));

            if (task.getDeadLine() != null) {
                Label deadline = new Label("Срок сдачи: " + task.getDeadLine().toString());
                taskLayout.add(deadline);
            }

            if(task.getDescription() != null && !task.getDescription().isEmpty()) {
                TextArea taskDescription = new TextArea();
                taskDescription.setValue(task.getDescription());
                taskDescription.getStyle().set("font-size", "var(--lumo-font-size-s)");
                taskDescription.setReadOnly(true);
                taskDescription.setWidthFull();
                taskDescription.setMaxHeight("200px");
                taskLayout.add(taskDescription);
            }

            appendResources(taskLayout, task.getResources());

            taskLayout.getStyle().set("width", "70%")
                    .set("border", "1px solid cadetblue")
                    .set("border-radius", "var(--lumo-border-radius-s)");

            layout.add(taskLayout);
        }
    }
}

