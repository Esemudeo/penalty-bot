package com.esemudeo.quarkus.penaltybot.configuration;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("error")
public class ErrorView extends VerticalLayout {

    public ErrorView() {
        add(new H2("Access denied"));
        add(new Paragraph("The link is invalid or has expired. Please request a new link using /penalty-setup."));
    }
}
