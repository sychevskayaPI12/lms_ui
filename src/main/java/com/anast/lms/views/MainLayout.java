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

public class MainLayout extends AppLayout {
    private final SecurityService securityService;

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
        div.setPadding(true);

        var mainHeader = new HorizontalLayout(div, userBar);
        mainHeader.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        mainHeader.setWidthFull();

        addToNavbar(mainHeader);

    }

    private void createDrawer() {

        //todo pretty tabs
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        Tab personalTab = new Tab(new RouterLink("Моя страница", MainView.class));
        Tab personalCoursesTab = new Tab(new RouterLink("Мои курсы", CoursesPage.class));

        tabs.add(personalTab, personalCoursesTab);

        addToDrawer(tabs);
    }
}
