package com.midvision.go.task.jobrunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.AbstractGoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

@Extension
public class RapidDeployJobTask extends AbstractGoPlugin {

	private static final Logger LOGGER = Logger.getLoggerFor(RapidDeployJobTask.class);

	public static final String EXTENSION_NAME = "task";
	public static final String SERVER_URL_PROPERTY = "Url";
	public static final String AUTH_TOKEN_PROPERTY = "Token";
	public static final String PROJECT_PROPERTY = "Project";
	public static final String TARGET_PROPERTY = "Target";
	public static final String PACKAGE_REPO_PROPERTY = "PackageRepo";
	public static final String PACKAGE_PROPERTY = "Package";

	@Override
	public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) throws UnhandledRequestTypeException {
		LOGGER.debug(String.format(">>> Request '%s': %s\n", requestMessage.requestName(), requestMessage.requestBody()));
		if ("configuration".equals(requestMessage.requestName())) {
			return handleGetConfigRequest();
		} else if ("validate".equals(requestMessage.requestName())) {
			return handleValidation(requestMessage);
		} else if ("execute".equals(requestMessage.requestName())) {
			return handleTaskExecution(requestMessage);
		} else if ("view".equals(requestMessage.requestName())) {
			return handleTaskView();
		}
		throw new UnhandledRequestTypeException(requestMessage.requestName());
	}

	@Override
	public GoPluginIdentifier pluginIdentifier() {
		return new GoPluginIdentifier(EXTENSION_NAME, Arrays.asList("1.0"));
	}

	/** AUXILIARY METHODS **/

	private GoPluginApiResponse handleTaskView() {
		int responseCode = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;
		HashMap<String, Object> view = new HashMap<String, Object>();
		view.put("displayValue", "RapidDeploy job runner");
		try {
			view.put("template", IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8"));
		} catch (Exception e) {
			responseCode = DefaultGoApiResponse.INTERNAL_ERROR;
			String errorMessage = "Failed to find template: " + e.getMessage();
			view.put("exception", errorMessage);
			LOGGER.error(errorMessage, e);
		}
		return createResponse(responseCode, view);
	}

	private GoPluginApiResponse handleTaskExecution(GoPluginApiRequest request) {
		RapidDeployJobTaskExecutor executor = new RapidDeployJobTaskExecutor();
		Map<?, ?> executionRequest = new GsonBuilder().create().fromJson(request.requestBody(), Map.class);
		Map<?, ?> config = (Map<?, ?>) executionRequest.get("config");
		Map<?, ?> context = (Map<?, ?>) executionRequest.get("context");
		Result result = executor.execute(new Config(config), new Context(context), JobConsoleLogger.getConsoleLogger());
		return createResponse(result.responseCode(), result.toMap());
	}

	private GoPluginApiResponse handleValidation(GoPluginApiRequest request) {
		HashMap<String, Object> validationResult = new HashMap<String, Object>();
		Map<?, ?> configMap = new GsonBuilder().create().fromJson(request.requestBody(), Map.class);
		HashMap<String, String> errorMap = new HashMap<String, String>();
		if (!configMap.containsKey(SERVER_URL_PROPERTY) || ((Map<?, ?>) configMap.get(SERVER_URL_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(SERVER_URL_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(SERVER_URL_PROPERTY, "The server URL cannot be empty");
		}
		if (!configMap.containsKey(AUTH_TOKEN_PROPERTY) || ((Map<?, ?>) configMap.get(AUTH_TOKEN_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(AUTH_TOKEN_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(AUTH_TOKEN_PROPERTY, "The authentication token cannot be empty");
		}
		if (!configMap.containsKey(PROJECT_PROPERTY) || ((Map<?, ?>) configMap.get(PROJECT_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(PROJECT_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(PROJECT_PROPERTY, "The project name cannot be empty");
		}
		if (!configMap.containsKey(TARGET_PROPERTY) || ((Map<?, ?>) configMap.get(TARGET_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(TARGET_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(TARGET_PROPERTY, "The target name cannot be empty");
		}
		if (!configMap.containsKey(PACKAGE_REPO_PROPERTY) || ((Map<?, ?>) configMap.get(PACKAGE_REPO_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(PACKAGE_REPO_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(PACKAGE_REPO_PROPERTY, "The package repository name cannot be empty");
		}
		if (!configMap.containsKey(PACKAGE_PROPERTY) || ((Map<?, ?>) configMap.get(PACKAGE_PROPERTY)).get("value") == null
				|| ((String) ((Map<?, ?>) configMap.get(PACKAGE_PROPERTY)).get("value")).trim().isEmpty()) {
			errorMap.put(PACKAGE_PROPERTY, "The package name cannot be empty");
		}
		validationResult.put("errors", errorMap);
		return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, validationResult);
	}

	private GoPluginApiResponse handleGetConfigRequest() {
		HashMap<String, Object> config = new HashMap<String, Object>();

		HashMap<String, Object> serverUrl = new HashMap<String, Object>();
		serverUrl.put("display-order", "0");
		serverUrl.put("display-name", "Server URL");
		serverUrl.put("required", true);
		config.put(SERVER_URL_PROPERTY, serverUrl);

		HashMap<String, Object> authToken = new HashMap<String, Object>();
		authToken.put("display-order", "1");
		authToken.put("display-name", "Authentication token");
		authToken.put("required", true);
		config.put(AUTH_TOKEN_PROPERTY, authToken);

		HashMap<String, Object> projectName = new HashMap<String, Object>();
		projectName.put("display-order", "2");
		projectName.put("display-name", "Project name");
		projectName.put("required", true);
		config.put(PROJECT_PROPERTY, projectName);

		HashMap<String, Object> targetName = new HashMap<String, Object>();
		targetName.put("display-order", "3");
		targetName.put("display-name", "Target name");
		targetName.put("required", true);
		config.put(TARGET_PROPERTY, targetName);

		HashMap<String, Object> packageRepoName = new HashMap<String, Object>();
		packageRepoName.put("display-order", "4");
		packageRepoName.put("display-name", "Package repository name");
		packageRepoName.put("required", true);
		config.put(PACKAGE_REPO_PROPERTY, packageRepoName);

		HashMap<String, Object> packageName = new HashMap<String, Object>();
		packageName.put("display-order", "5");
		packageName.put("display-name", "Package name");
		packageName.put("required", true);
		config.put(PACKAGE_PROPERTY, packageName);

		return createResponse(DefaultGoPluginApiResponse.SUCCESS_RESPONSE_CODE, config);
	}

	private GoPluginApiResponse createResponse(int responseCode, Map<String, Object> body) {
		final DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
		response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
		LOGGER.debug(String.format("<<< Response [%s]: %s\n", response.responseCode(), response.responseBody()));
		return response;
	}
}
