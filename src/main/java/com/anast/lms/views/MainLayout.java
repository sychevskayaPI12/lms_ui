package com.anast.lms.views;

import com.anast.lms.service.security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLink;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private Tabs tabs;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {

        String u = securityService.getAuthenticatedUser().getUsername();
        Button logout = new Button("Выход", e -> securityService.logout());
        Avatar avatarName = new Avatar(u);

        HorizontalLayout userBar = new HorizontalLayout(new Label(u), avatarName, logout);
        userBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userBar.setPadding(true);


        H1 header = new H1();
        header.setText("Система управления обучением");
        header.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");
        HorizontalLayout div = new HorizontalLayout(new DrawerToggle(), header);
        div.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        div.setWidthFull();
        div.setPadding(false);

        var mainHeader = new HorizontalLayout(div, userBar);
        mainHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainHeader.setWidthFull();

        addToNavbar(mainHeader);

    }

    private void createDrawer() {

        List<String> roles = securityService.getAuthenticatedUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        addTab("Моя страница", MainView.class);

        if(roles.contains("ROLE_MODERATOR")) {
            addTab("Заявки на регистрацию", RegistrationRequestsPage.class);
            addTab("Регистрация пользователя", RegistrationPage.class);
        }

        if(roles.contains("ROLE_TEACHER") || roles.contains("ROLE_STUDENT")) {
            addTab("Мои курсы", CoursesPage.class);
            addTab("Расписание", SchedulePage.class);
        }

        addToDrawer(tabs);
    }

    private void addTab(String title, Class pageClass) {
        Tab tab = new Tab(new RouterLink(title, pageClass));
        tabs.add(tab);
    }
}
