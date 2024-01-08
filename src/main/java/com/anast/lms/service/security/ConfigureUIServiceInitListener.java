package com.anast.lms.service.security;

import com.anast.lms.views.LoginView;
import com.anast.lms.views.RegistrationPage;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigureUIServiceInitListener implements VaadinServiceInitListener{

    private final List<Class> accessiblePages = List.of(LoginView.class, RegistrationPage.class);

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::authenticateNavigation);
        });
    }

    private void authenticateNavigation(BeforeEnterEvent event) {
        if (!accessiblePages.contains(event.getNavigationTarget())
                && !SecurityUtils.isUserLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
    }
}
