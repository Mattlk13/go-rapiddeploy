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

import org.apache.commons.io.IOUtils;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Task;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskConfigProperty;
import com.thoughtworks.go.plugin.api.task.TaskExecutor;
import com.thoughtworks.go.plugin.api.task.TaskView;

@Extension
public class RapidDeployJobTask implements Task {

    public static final String URL_PROPERTY = "Url";
    public static final String TOKEN_PROPERTY = "Token";
    public static final String PROJECT_PROPERTY = "Project";
    public static final String ENVIRONMENT_PROPERTY = "Environment";
    public static final String PACKAGE_REPO_PROPERTY = "PackageRepo";
    public static final String PACKAGE_PROPERTY = "Package";   

    @Override
    public TaskConfig config() {
        TaskConfig config = new TaskConfig();
        config.addProperty(URL_PROPERTY);
        config.addProperty(TOKEN_PROPERTY).with(TaskConfigProperty.SECURE, true);
        config.addProperty(PROJECT_PROPERTY);
        config.addProperty(ENVIRONMENT_PROPERTY);
        config.addProperty(PACKAGE_REPO_PROPERTY);
        config.addProperty(PACKAGE_PROPERTY);
        return config;
    }

    @Override
    public TaskExecutor executor() {
        return new RapidDeployJobTaskExecutor();
    }

    @Override
    public TaskView view() {
        TaskView taskView = new TaskView() {
            @Override
            public String displayValue() {
                return "RapidDeployJob";
            }

            @Override
            public String template() {
                try {
                    return IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8");
                } catch (Exception e) {
                    return "Failed to find template: " + e.getMessage();
                }
            }
        };
        return taskView;
    }

    @Override
    public ValidationResult validate(TaskConfig configuration) {
        ValidationResult validationResult = new ValidationResult();
        if (configuration.getValue(URL_PROPERTY) == null || configuration.getValue(URL_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(URL_PROPERTY, "URL cannot be empty"));
        } else if (configuration.getValue(TOKEN_PROPERTY) == null || configuration.getValue(TOKEN_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(TOKEN_PROPERTY, "Token cannot be empty"));
        } else if (configuration.getValue(PROJECT_PROPERTY) == null || configuration.getValue(PROJECT_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(PROJECT_PROPERTY, "Project name cannot be empty"));
        } else if (configuration.getValue(ENVIRONMENT_PROPERTY) == null || configuration.getValue(ENVIRONMENT_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(ENVIRONMENT_PROPERTY, "Environment name cannot be empty"));
        } else if (configuration.getValue(PACKAGE_REPO_PROPERTY) == null || configuration.getValue(PACKAGE_REPO_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(PACKAGE_REPO_PROPERTY, "Package repository name cannot be empty"));
        } else if (configuration.getValue(PACKAGE_PROPERTY) == null || configuration.getValue(PACKAGE_PROPERTY).trim().isEmpty()) {
            validationResult.addError(new ValidationError(PACKAGE_PROPERTY, "Package name cannot be empty"));
        }
        
        return validationResult;
    }
}
