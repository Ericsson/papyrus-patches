package com.ericsson.papyrus.devtools.views;

import java.time.LocalTime;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

public class RequestLogEntry implements IAdaptable {	
	private LocalTime time;
	private Request request;
	private EditPart host;
	private Command command;

	public RequestLogEntry(EditPart host, Request request, Command cmd) {
		super();
		this.time = LocalTime.now();
		this.host = host;
		this.request = request;
		this.command = cmd;
	}
	
	/**
	 * @return the millis
	 */
	public LocalTime getTime() {
		return time;
	}

	public String getTimeString() {
		return String.format("%02d:%02d:%02d:%03d",time.getHour(), time.getMinute(), time.getSecond(), time.getNano() / 1000000);
	}
	/**
	 * @return the request
	 */
	public Request getRequest() {
		return request;
	}

	/**
	 * @return the host
	 */
	public EditPart getHost() {
		return host;
	} 
	
	public Command getCommand() {
		return command;
	}

	public String toString() {
		String hostClassName = host.getClass().getName();
		hostClassName = hostClassName.substring(hostClassName.lastIndexOf('.')+1);

		String requestClassName = request.getClass().getName();
		requestClassName = requestClassName.substring(requestClassName.lastIndexOf('.')+1);
		return String.format("%s (%s) --> %s", 
			requestClassName, request.getType(), hostClassName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (EditPart.class.isAssignableFrom(adapter))
			return (T)host;			
		if (Request.class.isAssignableFrom(adapter))
			return (T)request;
		if (Command.class.isAssignableFrom(adapter))
			return (T)command;
		return null;
	}	

}


