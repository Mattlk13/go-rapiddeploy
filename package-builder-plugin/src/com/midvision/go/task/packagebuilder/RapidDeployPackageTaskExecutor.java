package com.midvision.go.task.packagebuilder;

import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

public class RapidDeployPackageTaskExecutor {

	public Result execute(final Config config, final Context context, final JobConsoleLogger console) {
		try {
			final String serverUrl = config.getServerUrl();
			final String authToken = config.getAuthToken();
			final String projectName = config.getProjectName();
			final String packageName = config.getPackageName();

			console.printLine("Starting RapidDeploy package build with parameters");
			console.printLine("**************************************************");
			console.printLine("URL: " + serverUrl);
			console.printLine("Authentication token: ******");
			console.printLine("Project name: " + projectName);
			console.printLine("Package name: " + packageName);

			final String output = RapidDeployConnector.invokeRapidDeployBuildPackage(authToken, serverUrl, projectName, packageName, "jar", true, false);
			console.printLine("Package build finished successfully, with output:\n " + output);
			return new Result(true, "Successfully built RapidDeploy package. Please check the output.");
		} catch (final Exception e) {
			return new Result(false, "Failed to invoke RapidDeploy job on URL: " + config.getServerUrl() + " \nError message: " + e.getMessage(), e);
		}
	}
}