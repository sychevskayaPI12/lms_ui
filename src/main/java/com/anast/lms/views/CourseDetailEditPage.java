package com.anast.lms.views;

import com.anast.lms.model.*;
import com.anast.lms.service.external.StudyServiceClient;
import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "/my_courses/:id?/edit", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CourseDetailEditPage extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final StudyServiceClient studyClient;
    private final SecurityService securityService;

    private Integer currentCourseId;
    private VerticalLayout modulesLayout;

    private final static String TITLE_FIELD_ID = "module_title";
    private final static String CONTENT_FIELD_ID = "module_content";


    public CourseDetailEditPage(StudyServiceClient studyClient, SecurityService securityService) {
        this.studyClient = studyClient;
        this.securityService = securityService;
        this.modulesLayout = new VerticalLayout();
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

        buildPanel();

        CourseFullInfoResponse courseDetailPage = studyClient.getCourseFullInfo(currentCourseId);
        Course course = courseDetailPage.getCourse();
        DisciplineInstance discipline = course.getDiscipline();

        //todo тут редактируемые элементы
        //как потом собирать их в объект данных? неужели на каждый навесить листенер? или дать идентификаторы и при Сохранить собрать / распарсить?

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

        Button addModule = new Button("Добавить модуль");
        addModule.addClickListener(e -> modulesLayout.add(buildModuleItem(null)));

        List<CourseModule> modules = courseDetailPage.getModules();
        modules.forEach(module -> modulesLayout.add(buildModuleItem(module)));

        add(addModule, modulesLayout);

        //если много контента, дублруем панель вниз
        if(modules.size() > 1) {
            buildPanel();
        }

    }


    private Details buildModuleItemOld(CourseModule module) {

        VerticalLayout layout = new VerticalLayout();
        TextArea content = new TextArea();
        content.setValue(module.getContent());
        content.setReadOnly(true);
        content.setWidthFull();
        content.setMaxHeight("400px");

        layout.add(content);

        for (ModuleResource resource : module.getResources()) {

            StreamResource streamResource = null;
            try {
                FileInputStream fileInputStream = new FileInputStream(resource.getFile());
                streamResource = new StreamResource(resource.getDisplayFileName(), (InputStreamFactory) () -> fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Anchor anchor = new Anchor(streamResource, resource.getDisplayFileName());
            anchor.setTarget( "_blank" );  // Specify `_blank` to open in a new browser tab/window.
            layout.add(anchor);

        }

        //layout.getStyle().set("border-top", "1px solid cadetblue");
        layout.setWidthFull();

        Details details = new Details(module.getTitle(), layout);
        details.setOpened(true);
        details.getStyle().set("width", "70%")
                .set("border-top", "1px solid cadetblue")
                .set("font-weight", "bold");


        return details;
    }

    private VerticalLayout buildModuleItem(CourseModule module) {
        VerticalLayout layout = new VerticalLayout();

        TextField moduleTitle = new TextField();
        moduleTitle.setPlaceholder("Название модуля");
        moduleTitle.setWidthFull();
        moduleTitle.setId(TITLE_FIELD_ID);

        TextArea content = new TextArea();
        content.setPlaceholder("Детали курса");
        content.setWidthFull();
        content.setMaxHeight("400px");
        content.setId(CONTENT_FIELD_ID);

        if(module != null) {
            layout.setId(module.getId().toString());
            moduleTitle.setValue(module.getTitle());
            content.setValue(module.getContent());
        }

        layout.add(moduleTitle, content);

        layout.getStyle().set("width", "70%")
                .set("border", "1px solid cadetblue")
                .set("border-radius", "var(--lumo-border-radius-s)");
        return layout;
    }


    void buildPanel() {

        Button done = new Button("Сохранить");
        done.addClickListener(e -> {
           List<CourseModule> updatedModules = getUpdatedModulesData();
           try{
               studyClient.updateCourseModules(currentCourseId, updatedModules);
               done.getUI().ifPresent(ui -> ui.navigate(
                       CourseDetailPage.class, currentCourseId));
           } catch (Exception ex) {
               Notification notification = new Notification("Ошибка при сохранении. Детали: " + ex.getMessage());
               notification.open();
           }
        });

        Button cancel = new Button("Отмена");
        cancel.addClickListener(e -> cancel.getUI().ifPresent(ui -> ui.navigate(
                CourseDetailPage.class, currentCourseId)
        ));

        HorizontalLayout layout = new HorizontalLayout(done, cancel);
        add(layout);
    }

    private List<CourseModule> getUpdatedModulesData() {
        List<CourseModule> updatedModules = new ArrayList<>();
        //todo ресурсы
        var counterRef = new Object() {
            short orderCount = 1;
        };

        modulesLayout.getChildren().forEach(moduleLayout -> {

            TextField titleComponent = (TextField) moduleLayout.getChildren().filter(c -> c.getId().get().equals(TITLE_FIELD_ID)).findAny().get();
            TextArea contentComponent = (TextArea) moduleLayout.getChildren().filter(c -> c.getId().get().equals(CONTENT_FIELD_ID)).findAny().get();

            Integer moduleId = moduleLayout.getId().isPresent() ? Integer.valueOf(moduleLayout.getId().get()) : null;
            String title = titleComponent.getValue();
            String content = contentComponent.getValue();


            CourseModule module = new CourseModule(moduleId, title, content, counterRef.orderCount, new ArrayList<>());
            updatedModules.add(module);
            counterRef.orderCount++;
        });
        return updatedModules;
    }
}

