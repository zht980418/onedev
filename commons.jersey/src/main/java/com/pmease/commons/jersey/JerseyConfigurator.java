package com.pmease.commons.jersey;

import org.glassfish.jersey.server.ResourceConfig;

public interface JerseyConfigurator {
	void configure(ResourceConfig resourceConfig);
}
