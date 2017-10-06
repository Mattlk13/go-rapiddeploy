package com.midvision.go.task.jobrunner;

import java.util.Map;

import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;

public class RapidDeployJobTaskExecutor {

	public Result execute(final Config config, final Context context, final JobConsoleLogger console) {
		try {
			final String serverUrl = config.getServerUrl();
			final String authToken = config.getAuthToken();
			final String project = config.getProject();
			final String target = config.getTarget();
			final String packageRepoName = config.getPackageRepoName();
			final String packageName = config.getPackageName();

			console.printLine("Starting RapidDeploy job with parameters");
			console.printLine("****************************************");
			console.printLine("Server URL: " + serverUrl);
			console.printLine("Authentication token: ******");
			console.printLine("Project name: " + project);
			console.printLine("Target name: " + target);
			console.printLine("Package repository name: " + packageRepoName);
			console.printLine("Package name: " + packageName);

			final String deploymentPackageNameKey = String.format("GO_PACKAGE_%s_LABEL", packageRepoName + ":" + packageName).replaceAll("[^A-Za-z0-9_]", "_")
					.toUpperCase();

			console.printLine("Looking up selected package material artefact in environment variables with key '" + deploymentPackageNameKey + "'");

			@SuppressWarnings("unchecked")
			final Map<String, String> envMap = (Map<String, String>) context.getEnvironmentVariables();
			final String deploymentPackageName = envMap.get(deploymentPackageNameKey);

			if (deploymentPackageName == null || "".equals(deploymentPackageName)) {
				return new Result(false, "Could not find deployment package!\n"
						+ "Please check if the package repository and package name match with the configuration of the package material plugin.");
			} else {
				console.printLine("Found deployment package name: " + deploymentPackageName);
			}

			final String output = RapidDeployConnector.invokeRapidDeployDeploymentPollOutput(authToken, serverUrl, project, target, deploymentPackageName,
					false, true);

			final String jobId = RapidDeployConnector.extractJobId(output);
			boolean success = true;
			if (jobId != null) {
				console.printLine("Checking job status every 30 seconds...");
				boolean runningJob = true;
				// sleep 30sec by default
				long milisToSleep = 30000;
				while (runningJob) {
					Thread.sleep(milisToSleep);
					final String jobDetails = RapidDeployConnector.pollRapidDeployJobDetails(authToken, serverUrl, jobId);
					final String jobStatus = RapidDeployConnector.extractJobStatus(jobDetails);

					console.printLine("Job status is " + jobStatus);
					if (jobStatus.equals("DEPLOYING") || jobStatus.equals("QUEUED") || jobStatus.equals("STARTING") || jobStatus.equals("EXECUTING")) {
						console.printLine("Job running, next check in 30 seconds...");
						milisToSleep = 30000;
					} else if (jobStatus.equals("REQUESTED") || jobStatus.equals("REQUESTED_SCHEDULED")) {
						console.printLine(
								"Job in a REQUESTED state. Approval may be required in RapidDeploy to continue with the execution, next check in 30 seconds...");
					} else if (jobStatus.equals("SCHEDULED")) {
						console.printLine("Job in a SCHEDULED state, execution will start in a future date, next check in 5 minutes...");
						console.printLine("Printing out job details:");
						console.printLine(jobDetails);
						milisToSleep = 300000;
					} else {
						runningJob = false;
						console.printLine("Job finished with status " + jobStatus);
						if (jobStatus.equals("FAILED") || jobStatus.equals("REJECTED") || jobStatus.equals("CANCELLED") || jobStatus.equals("UNEXECUTABLE")
								|| jobStatus.equals("TIMEDOUT") || jobStatus.equals("UNKNOWN")) {
							success = false;
						}
					}
				}
			} else {
				throw new Exception("Could not retrieve job id, running asynchronously!");
			}
			console.printLine(System.getProperty("line.separator"));
			final String logs = RapidDeployConnector.pollRapidDeployJobLog(authToken, serverUrl, jobId);
			console.printLine(logs);

			if (!success) {
				return new Result(false, "Failed to run RapidDeploy job. Please check the output.");
			}

			return new Result(true, "Successfully run RapidDeploy job. Please check the output.");
		} catch (final Exception e) {
			return new Result(false, "Failed to invoke RapidDeploy job on URL: " + config.getServerUrl() + " \nError message: " + e.getMessage(), e);
		}
	}
}