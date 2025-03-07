package com.jordan.airportweather.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.jordan.airportweather.client.event.NavEvent;
import com.jordan.airportweather.client.event.NavEventHandler;

public class AppController implements ValueChangeHandler<String>, NavEventHandler {
	// history tags
	private final String tagPage1 = "page1";
	private final String tagPage2 = "page2";

	//private static Logger logger = Logger.getLogger("AppController");

	private EventBus eventBus;
	private HasWidgets container;
	
	public AppController() {
		this.eventBus = new SimpleEventBus();

        // add this class to handle navigation request events
        eventBus.addHandler(NavEvent.TYPE, this);
        
        // add this class to handle history change events
        History.addValueChangeHandler(this);
	}
	
	public void setContainer(HasWidgets container) {
		this.container = container;
	}

    public void launchStartPage()
    {
        if ("".equals(History.getToken())) {
            History.newItem(tagPage1);
        }
        else {
            History.fireCurrentHistoryState();
        }
    }

	// this handles history change events
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String token = event.getValue();
		if (token != null) {
			Composite view = null;
			
			if (token.equals(tagPage1)) {
		        view = new TestUi(eventBus, "Page 1");
			}
			else if (token.equals(tagPage2)) {
                view = new TestUi(eventBus, "Page 2");
			}
			
			if (view != null) {
		        container.clear();
				container.add(view);
			}
		}
	}

    @Override
    public void onNavEvent(NavEvent event) {
        NavEvent.Page destPage = event.getDestPage();
        if (destPage == NavEvent.Page.Page1) {
            History.newItem(tagPage1);
        }
        else if (destPage == NavEvent.Page.Page2) {
            History.newItem(tagPage2);
        }
        else if (destPage == NavEvent.Page.Back) {
            History.back();
        }
    }
}
