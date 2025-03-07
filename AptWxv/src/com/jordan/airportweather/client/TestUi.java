/**
 * 
 */
package com.jordan.airportweather.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.jordan.airportweather.client.event.NavEvent;

import gwt.material.design.client.ui.MaterialButton;

public class TestUi extends Composite {

    private static TestUiUiBinder uiBinder = GWT.create(TestUiUiBinder.class);

    interface TestUiUiBinder extends UiBinder<Widget, TestUi> {
    }

    private EventBus eventBus;

    /**
     * Because this class has a default constructor, it can
     * be used as a binder template. In other words, it can be used in other
     * *.ui.xml files as follows:
     * <ui:UiBinder xmlns:ui="urn:ui:com.google.gwt.uibinder"
      *   xmlns:g="urn:import:**user's package**">
     *  <g:**UserClassName**>Hello!</g:**UserClassName>
     * </ui:UiBinder>
     * Note that depending on the widget that is used, it may be necessary to
     * implement HasHTML instead of HasText.
     */
    public TestUi(EventBus eventBus, String buttonText) {
        this.eventBus = eventBus;

        initWidget(uiBinder.createAndBindUi(this));
        
        button.setText(buttonText);
    }

    @UiField
    MaterialButton button;

    @UiHandler("button")
    void onClick(ClickEvent e) {
        eventBus.fireEvent(new NavEvent(NavEvent.Page.Page2));
    }

}
