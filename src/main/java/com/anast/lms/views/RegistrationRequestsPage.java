package com.anast.lms.views;

import com.anast.lms.model.RequestState;
import com.anast.lms.model.profile.RegistrationRequest;
import com.anast.lms.service.external.ModerationServiceClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "/registration_moderation", layout=MainLayout.class)
@PageTitle("Заявки на регистрацию | LMS")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class RegistrationRequestsPage extends VerticalLayout {

    private final ModerationServiceClient moderationServiceClient;

    private Select<RequestState> requestStateSelect;
    private Grid<RegistrationRequest> grid;

    public RegistrationRequestsPage(ModerationServiceClient moderationServiceClient) {
        this.moderationServiceClient = moderationServiceClient;

        Label h = new Label("Управление заявками на регистрацию пользователей");
        h.getStyle().set("font-size", "var(--lumo-font-size-l)");
        add(h);
        initSelect();
        buildGrid();
    }

    private void initSelect() {
        requestStateSelect = new Select<>();
        requestStateSelect.setItems(RequestState.values());
        requestStateSelect.setItemLabelGenerator(RequestState::getTitle);

        requestStateSelect.setValue(RequestState.unprocessed);
        requestStateSelect.addValueChangeListener(e -> fillRequests());
        add(requestStateSelect);
    }

    private void buildGrid() {
        grid = new Grid<>();

        grid.addColumn(RegistrationRequest::getId).setHeader("№").setAutoWidth(true);
        grid.addColumn(r->r.getCreateDate().toLocalDate()).setHeader("Дата обращения").setSortable(true).setAutoWidth(true);
        grid.addColumn(this::getRolesDisplayString).setHeader("Пользователь").setAutoWidth(true).setSortable(true);
        grid.addColumn(RegistrationRequest::getDisplayName).setHeader("ФИО").setAutoWidth(true);
        grid.addColumn(RegistrationRequest::getUserLogin).setHeader("Логин пользователя").setAutoWidth(true);

        fillRequests();
        grid.setWidth("80%");

        grid.addSelectionListener(e-> {
            Optional<RegistrationRequest> selected = e.getFirstSelectedItem();
            if(selected.isPresent() && selected.get().getRequestStateEnum().equals(RequestState.unprocessed)) {
                createAndShowDialog(selected.get());
            }
        });
        add(grid);
    }

    private void createAndShowDialog(RegistrationRequest request) {
        Dialog dialog = new Dialog();
        dialog.setWidth("40%");
        VerticalLayout dialogLayout = createDialogLayout(dialog, request);
        dialog.add(dialogLayout);

        dialog.open();
    }

    private VerticalLayout createDialogLayout(Dialog dialog, RegistrationRequest request) {
        H2 headline = new H2("Заявка на регистрацию №" + request.getId());
        headline.getStyle().set("margin", "var(--lumo-space-m) 0 0 0")
                .set("font-size", "1.5em").set("font-weight", "bold");

        VerticalLayout fieldLayout = getDialogFieldsLayout(request);

        Button acceptButton = new Button("Принять", new Icon(VaadinIcon.CHECK));
        acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        acceptButton.addClickListener(c-> {
            moderationServiceClient.acceptRegistrationRequest(request.getId());
            fillRequests();
            dialog.close();
        });

        Button declineButton = new Button("Отклонить", new Icon(VaadinIcon.CLOSE));
        declineButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        declineButton.addClickListener(c-> {
            moderationServiceClient.declineRegistrationRequest(request.getId());
            fillRequests();
            dialog.close();
        });

        Button cancelButton = new Button("Отмена", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(acceptButton, declineButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(headline, fieldLayout,
                buttonLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        dialogLayout.setWidthFull();

        return dialogLayout;

    }

    private void fillRequests() {

        List<RegistrationRequest> requestList = moderationServiceClient.getRegistrationRequests(requestStateSelect.getValue().getValue());
        grid.setItems(requestList);
    }

    private String getRolesDisplayString(RegistrationRequest request) {
        List<String> roles = new ArrayList<>();
        if(request.getUserProfile().getTeacherInfo() != null) {
            roles.add("Преподаватель");
        }
        if(request.getUserProfile().getStudentInfo() != null) {
            roles.add("Студент");
        }
        return String.join(", ", roles);
    }

    private VerticalLayout getDialogFieldsLayout(RegistrationRequest request) {

        Label nameLabel = new Label("ФИО: " + request.getDisplayName());
        Label mailLabel = new Label("Почта: " + request.getUserProfile().getUserProfileInfo().getMail());

        VerticalLayout fieldLayout = new VerticalLayout(nameLabel, mailLabel);

        if(request.getUserProfile().getStudentInfo() != null) {
            Label student = new Label(request.getUserProfile().getStudentInfo().toString());
            fieldLayout.add(student);
        }

        if(request.getUserProfile().getTeacherInfo() != null) {
            request.getUserProfile().getTeacherInfo().getPositions().forEach(p -> {
                Label teacher = new Label(p.toString());
                fieldLayout.add(teacher);
            });
        }

        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        return fieldLayout;
    }

}
