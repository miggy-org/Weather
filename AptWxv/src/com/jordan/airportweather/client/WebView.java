package com.jordan.airportweather.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class WebView implements EntryPoint {

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        AppController appcontroller = new AppController();
        appcontroller.setContainer(RootPanel.get());
        appcontroller.launchStartPage();
    }
}
