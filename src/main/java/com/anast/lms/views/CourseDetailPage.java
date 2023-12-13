package com.anast.lms.views;

import com.anast.lms.model.*;
import com.anast.lms.service.external.StudyServiceClient;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;

import java.util.List;

@Route(value = "/my_courses/:id?/view", layout=MainLayout.class)
@PageTitle("Мои курсы | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class CourseDetailPage extends VerticalLayout implements HasUrlParameter<Integer>, BeforeEnterObserver {

    private final StudyServiceClient studyClient;

    private Integer currentCourseId;

    public CourseDetailPage(StudyServiceClient studyClient) {
        this.studyClient = studyClient;

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

        CourseFullInfoResponse courseDetailPage = studyClient.getCourseFullInfo(currentCourseId);
        Course course = courseDetailPage.getCourse();
        DisciplineInstance discipline = course.getDiscipline();

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

    private Details buildModuleItem(CourseModule module) {

        VerticalLayout layout = new VerticalLayout();
        TextArea content = new TextArea();
        content.setValue(module.getContent());
        content.setReadOnly(true);
        content.setWidthFull();
        content.setMaxHeight("400px");

        layout.add(content);

        //layout.getStyle().set("border-top", "1px solid cadetblue");
        layout.setWidthFull();

        Details details = new Details(module.getTitle(), layout);
        details.setOpened(true);
        details.getStyle().set("width", "70%")
                .set("border-top", "1px solid cadetblue")
                .set("font-weight", "bold");


        return details;
    }


}

