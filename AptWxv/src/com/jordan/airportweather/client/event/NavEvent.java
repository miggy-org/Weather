package com.jordan.airportweather.client.event;

import com.google.gwt.event.shared.GwtEvent;

// using this event for page navigation, will probably create a new event later for this
public class NavEvent extends GwtEvent<NavEventHandler> {

	public static Type<NavEventHandler> TYPE = new Type<NavEventHandler>();
	public enum Page { Page1, Page2, Back };
	private Page destPage;
	
	public NavEvent(Page destPage) {
		this.destPage = destPage;
	}

	public Page getDestPage() {
		return destPage;
	}
	
	@Override
	protected void dispatch(NavEventHandler handler) {
		handler.onNavEvent(this);
	}

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<NavEventHandler> getAssociatedType() {
        return TYPE;
    }
}
