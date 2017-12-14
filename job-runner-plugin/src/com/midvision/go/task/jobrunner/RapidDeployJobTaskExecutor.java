package com.midvision.go.task.jobrunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

			final Map<String, String> dataDictionary = new HashMap<String, String>();
			String dictValue = "";
			for (final Entry<String, String> envVar : envMap.entrySet()) {
				final Pattern pattern = Pattern.compile("@@.+@@");
				final Matcher matcher = pattern.matcher(envVar.getKey());
				if (matcher.matches()) {
					dictValue = replaceEnviromentVariables(envVar.getValue(), envMap, console);
					dataDictionary.put(envVar.getKey(), dictValue);
				}
			}
			console.printLine("Data dictionary: " + dataDictionary);

			final String output = RapidDeployConnector.invokeRapidDeployDeploymentPollOutput(authToken, serverUrl, project, target, deploymentPackageName,
					false, true, dataDictionary);

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

	private String replaceEnviromentVariables(String paramStr, final Map<String, String> envMap, final JobConsoleLogger console) {
		console.printLine("Replacing environment variables for '" + paramStr + "'");

		// First we need to retrieve all the placeholders: '${xxx}'
		final Pattern pattern = Pattern.compile("\\$\\{[^\\$\\{\\}]+\\}");
		// Then we need to extract the string inside the placeholder
		final Pattern inPattern = Pattern.compile("\\$\\{(.+)\\}");

		String group;
		String replaceStr;
		final Matcher matcher = pattern.matcher(paramStr);
		Matcher inMatcher;

		// We iterate over the placeholders found
		while (matcher.find()) {
			group = matcher.group();
			inMatcher = inPattern.matcher(group);
			// Obtain the string inside the placeholder
			if (inMatcher.matches()) {
				try {
					// Get the value of the parameter
					replaceStr = envMap.get(inMatcher.group(1));
					// If the value is not blank, replace the variable
					if (replaceStr != null) {
						console.printLine("    Retrieved value '" + replaceStr + "' from environment variable '" + group + "'");
						paramStr = paramStr.replace(group, replaceStr);
					} else {
						console.printLine("    WARNING: environment variable not found '" + group + "'");
					}
				} catch (final Exception e) {
					console.printLine("WARNING: Unable to retrieve the environment variable '" + group + "'");
					console.printLine("         " + e.getMessage());
				}
			}
		}
		console.printLine("Replaced value '" + paramStr + "'");
		return paramStr;
	}
}