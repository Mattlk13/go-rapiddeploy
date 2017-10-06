package com.midvision.go.task.packagebuilder;

import java.util.Map;

public class Config {
	public static final String URL_PROPERTY = "Url";
	public static final String TOKEN_PROPERTY = "Token";
	public static final String PROJECT_PROPERTY = "Project";

	private final String serverUrl;
	private final String authToken;
	private final String projectName;
	private final String packageName;

	public Config(Map<?, ?> config) {
		serverUrl = getValue(config, RapidDeployPackageTask.SERVER_URL_PROPERTY);
		authToken = getValue(config, RapidDeployPackageTask.AUTH_TOKEN_PROPERTY);
		projectName = getValue(config, RapidDeployPackageTask.PROJECT_PROPERTY);
		packageName = getValue(config, RapidDeployPackageTask.PACKAGE_PROPERTY);
	}

	private String getValue(Map<?, ?> config, String property) {
		return (String) ((Map<?, ?>) config.get(property)).get("value");
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getPackageName() {
		return packageName;
	}
}