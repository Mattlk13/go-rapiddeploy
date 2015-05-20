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
    public ExecutionResult execute(TaskConfig config, TaskExecutionContext taskEnvironment) {
        try {
            return runCommand(taskEnvironment, config);
        } catch (Exception e) {
            return ExecutionResult.failure("Failed to invoke RapidDeploy job on URL: " + config.getValue(RapidDeployJobTask.URL_PROPERTY) + " \nError message: " + e.getMessage(), e);
        }
    }

    private ExecutionResult runCommand(TaskExecutionContext taskContext, TaskConfig taskConfig) throws Exception {    	
        String url = taskConfig.getValue(RapidDeployJobTask.URL_PROPERTY);
        String token = taskConfig.getValue(RapidDeployJobTask.TOKEN_PROPERTY);
        String project = taskConfig.getValue(RapidDeployJobTask.PROJECT_PROPERTY);
        String environment = taskConfig.getValue(RapidDeployJobTask.ENVIRONMENT_PROPERTY);
        String packageRepoName = taskConfig.getValue(RapidDeployJobTask.PACKAGE_REPO_PROPERTY);
        String packageName = taskConfig.getValue(RapidDeployJobTask.PACKAGE_PROPERTY);
    	
    	Console console = taskContext.console();
    	boolean success = true;
        
    	console.printLine("Starting RapidDeploy job with parameters:");
    	console.printLine("Url: " + url);
    	console.printLine("Authentication token: ******");
    	console.printLine("Project name: " + project);
    	console.printLine("Environment name: " + environment);
    	console.printLine("Package repository name: " + packageRepoName);
    	console.printLine("Package name: " + packageName);
    	
    	String deploymentPackageNameKey = "GO_PACKAGE_" + packageRepoName.toUpperCase() + "_" + packageName.toUpperCase() + "_LABEL";
    	
    	console.printLine("Looking up selected package material artefact in environment variables with key '" + deploymentPackageNameKey + "'");    	
    	  	    	    	
    	Map<String,String> envMap = taskContext.environment().asMap();
    	String deploymentPackageName = envMap.get(deploymentPackageNameKey);
    	
    	if(deploymentPackageName == null || "".equals(deploymentPackageName)){
    		return ExecutionResult.failure("Could not find deployment package!\nPlease check if the package repository and package name matches with the configuration of the package material plugin.");
    	} else{
    		console.printLine("Found deployment package name: " + deploymentPackageName);
    	}
    	
		String output = RapidDeployConnector.invokeRapidDeployDeploymentPollOutput(token, url, project, environment, deploymentPackageName, true);
		
		String jobId = RapidDeployConnector.extractJobId(output);
		if(jobId != null){
			console.printLine("Checking job status in every 30 seconds...");
			boolean runningJob = true;
			//sleep 30sec by default
			long milisToSleep = 30000;
			while(runningJob){
				Thread.sleep(milisToSleep);
				String jobDetails = RapidDeployConnector.pollRapidDeployJobDetails(token, url, jobId);
				String jobStatus = RapidDeployConnector.extractJobStatus(jobDetails);
				
				console.printLine("Job status is " + jobStatus);
				if(jobStatus.equals("DEPLOYING") || jobStatus.equals("QUEUED") || 
						jobStatus.equals("STARTING") || jobStatus.equals("EXECUTING")){														
					console.printLine("Job is running, next check in 30 seconds..");
					milisToSleep = 30000;
				} else if(jobStatus.equals("REQUESTED") || jobStatus.equals("REQUESTED_SCHEDULED")){
					console.printLine("Job is in a REQUESTED state. Approval may be required in RapidDeploy to continue with execution, next check in 30 seconds..");
				} else if(jobStatus.equals("SCHEDULED")){
					console.printLine("Job is in a SCHEDULED state, execution will start in a future date, next check in 5 minutes..");
					console.printLine("Printing out job details");
					console.printLine(jobDetails);
					milisToSleep = 300000;
				} else{
				
					runningJob = false;
					console.printLine("Job is finished with status " + jobStatus);
					if(jobStatus.equals("FAILED") || jobStatus.equals("REJECTED") || 
							jobStatus.equals("CANCELLED") || jobStatus.equals("UNEXECUTABLE") || 
							jobStatus.equals("TIMEDOUT") || jobStatus.equals("UNKNOWN")){
						success = false;
					}
				}
			}
		} else{
			throw new Exception("Could not retrieve job id, running asynchronously!");
		}
		console.printLine("");
		String logs = RapidDeployConnector.pollRapidDeployJobLog(token, url, jobId);
		console.printLine(logs); 	        
                
        if (!success) {
            return ExecutionResult.failure("Failed to run RapidDeploy job. Please check the output.");
        }

        return ExecutionResult.success("Successfully ran RapidDeploy job. Please check the output.");
    }  
}