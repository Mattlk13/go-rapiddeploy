/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.midvision.go.task.jobrunner;

import java.util.Map;

import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;

public class RapidDeployJobTaskExecutor implements TaskExecutor {

	@Override
	public ExecutionResult execute(final TaskConfig config, final TaskExecutionContext taskEnvironment) {
		try {
			return runCommand(taskEnvironment, config);
		} catch (final Exception e) {
			return ExecutionResult.failure("Failed to invoke RapidDeploy job on URL: " + config.getValue(RapidDeployJobTask.URL_PROPERTY)
					+ " \nError message: " + e.getMessage(), e);
		}
	}

	private ExecutionResult runCommand(final TaskExecutionContext taskContext, final TaskConfig taskConfig) throws Exception {
		final String url = taskConfig.getValue(RapidDeployJobTask.URL_PROPERTY);
		final String token = taskConfig.getValue(RapidDeployJobTask.TOKEN_PROPERTY);
		final String project = taskConfig.getValue(RapidDeployJobTask.PROJECT_PROPERTY);
		final String environment = taskConfig.getValue(RapidDeployJobTask.ENVIRONMENT_PROPERTY);
		final String packageRepoName = taskConfig.getValue(RapidDeployJobTask.PACKAGE_REPO_PROPERTY);
		final String packageName = taskConfig.getValue(RapidDeployJobTask.PACKAGE_PROPERTY);

		final Console console = taskContext.console();
		boolean success = true;

		console.printLine("Starting RapidDeploy job with parameters:");
		console.printLine("URL: " + url);
		console.printLine("Authentication token: ******");
		console.printLine("Project name: " + project);
		console.printLine("Environment name: " + environment);
		console.printLine("Package repository name: " + packageRepoName);
		console.printLine("Package name: " + packageName);

		final String deploymentPackageNameKey = String.format("GO_PACKAGE_%s_LABEL", packageRepoName + ":" + packageName).replaceAll("[^A-Za-z0-9_]", "_")
				.toUpperCase();

		console.printLine("Looking up selected package material artefact in environment variables with key '" + deploymentPackageNameKey + "'");

		final Map<String, String> envMap = taskContext.environment().asMap();
		final String deploymentPackageName = envMap.get(deploymentPackageNameKey);

		if (deploymentPackageName == null || "".equals(deploymentPackageName)) {
			return ExecutionResult.failure("Could not find deployment package!\n"
					+ "Please check if the package repository and package name matches with the configuration of the package material plugin.");
		} else {
			console.printLine("Found deployment package name: " + deploymentPackageName);
		}

		final String output = RapidDeployConnector.invokeRapidDeployDeploymentPollOutput(token, url, project, environment, deploymentPackageName, false, true);

		final String jobId = RapidDeployConnector.extractJobId(output);
		if (jobId != null) {
			console.printLine("Checking job status every 30 seconds...");
			boolean runningJob = true;
			// sleep 30sec by default
			long milisToSleep = 30000;
			while (runningJob) {
				Thread.sleep(milisToSleep);
				final String jobDetails = RapidDeployConnector.pollRapidDeployJobDetails(token, url, jobId);
				final String jobStatus = RapidDeployConnector.extractJobStatus(jobDetails);

				console.printLine("Job status is " + jobStatus);
				if (jobStatus.equals("DEPLOYING") || jobStatus.equals("QUEUED") || jobStatus.equals("STARTING") || jobStatus.equals("EXECUTING")) {
					console.printLine("Job running, next check in 30 seconds...");
					milisToSleep = 30000;
				} else if (jobStatus.equals("REQUESTED") || jobStatus.equals("REQUESTED_SCHEDULED")) {
					console.printLine("Job in a REQUESTED state. Approval may be required in RapidDeploy to continue with the execution, next check in 30 seconds...");
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
		final String logs = RapidDeployConnector.pollRapidDeployJobLog(token, url, jobId);
		console.printLine(logs);

		if (!success) {
			return ExecutionResult.failure("Failed to run RapidDeploy job. Please check the output.");
		}

		return ExecutionResult.success("Successfully ran RapidDeploy job. Please check the output.");
	}
}