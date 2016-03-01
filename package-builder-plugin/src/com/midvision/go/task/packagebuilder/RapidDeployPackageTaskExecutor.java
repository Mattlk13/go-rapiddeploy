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

package com.midvision.go.task.packagebuilder;

import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;


public class RapidDeployPackageTaskExecutor implements TaskExecutor {

    @Override
    public ExecutionResult execute(TaskConfig config, TaskExecutionContext taskEnvironment) {
        try {
            return runCommand(taskEnvironment, config);
        } catch (Exception e) {
            return ExecutionResult.failure("Failed to invoke RapidDeploy job on URL: " + config.getValue(RapidDeployPackageTask.URL_PROPERTY) + " \nError message: " + e.getMessage(), e);
        }
    }

    private ExecutionResult runCommand(TaskExecutionContext taskContext, TaskConfig taskConfig) throws Exception {    	
        String url = taskConfig.getValue(RapidDeployPackageTask.URL_PROPERTY);
        String token = taskConfig.getValue(RapidDeployPackageTask.TOKEN_PROPERTY);
        String project = taskConfig.getValue(RapidDeployPackageTask.PROJECT_PROPERTY);        
    	
    	Console console = taskContext.console();
        
    	console.printLine("Starting RapidDeploy package build with parameters:");
    	console.printLine("URL: " + url);
    	console.printLine("Authentication token: ******");
    	console.printLine("Project name: " + project);    	    	
    	
    	String output;
    	output = RapidDeployConnector.invokeRapidDeployBuildPackage(token, url, project, null, "jar", true);
    	console.printLine("Package build finished successfully, with output:\n " + output);
        return ExecutionResult.success("Successfully built RapidDeploy package. Please check the output.");
    }  
}