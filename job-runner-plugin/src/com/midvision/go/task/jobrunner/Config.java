package com.midvision.go.task.jobrunner;

import java.util.Map;

public class Config {
	public static final String URL_PROPERTY = "Url";
	public static final String TOKEN_PROPERTY = "Token";
	public static final String PROJECT_PROPERTY = "Project";
	public static final String ENVIRONMENT_PROPERTY = "Environment";
	public static final String PACKAGE_REPO_PROPERTY = "PackageRepo";
	public static final String PACKAGE_PROPERTY = "Package";

	private final String serverUrl;
	private final String authToken;
	private final String project;
	private final String target;
	private final String packageRepo;
	private final String packageName;

	public Config(Map<?, ?> config) {
		serverUrl = getValue(config, RapidDeployJobTask.SERVER_URL_PROPERTY);
		authToken = getValue(config, RapidDeployJobTask.AUTH_TOKEN_PROPERTY);
		project = getValue(config, RapidDeployJobTask.PROJECT_PROPERTY);
		target = getValue(config, RapidDeployJobTask.TARGET_PROPERTY);
		packageRepo = getValue(config, RapidDeployJobTask.PACKAGE_REPO_PROPERTY);
		packageName = getValue(config, RapidDeployJobTask.PACKAGE_PROPERTY);
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

	public String getProject() {
		return project;
	}

	public String getTarget() {
		return target;
	}

	public String getPackageRepoName() {
		return packageRepo;
	}

	public String getPackageName() {
		return packageName;
	}
}