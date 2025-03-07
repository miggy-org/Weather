package com.jordan.airportweather.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface NavEventHandler extends EventHandler {
    void onNavEvent(NavEvent event);
}
