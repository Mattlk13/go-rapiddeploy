package com.tw.go.plugin.packagematerial;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;

public class RapidDeployRepositoryConfiguration implements PackageMaterialConfiguration{
	public static final String SERVER_URL = "URL";
	public static final String AUTH_TOKEN = "TOKEN";
	public static final String PROJECT_NAME = "PROJECT";
	public static final String PACKAGE_ID = "PACKAGE_ID";

	@Override
	public PackageConfiguration getPackageConfiguration() {
		 PackageConfiguration packageConfiguration = new PackageConfiguration();
	        packageConfiguration.add(new PackageMaterialProperty(PACKAGE_ID).with(PackageMaterialProperty.DISPLAY_NAME, "Package identifier").with(PackageMaterialProperty.DISPLAY_ORDER, 0));
	        return packageConfiguration;
	}

	@Override
	public RepositoryConfiguration getRepositoryConfiguration() {
		RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        repositoryConfiguration.add(new PackageMaterialProperty(SERVER_URL).with(PackageMaterialProperty.DISPLAY_NAME, "RapidDeploy url").with(PackageMaterialProperty.DISPLAY_ORDER, 0));
        repositoryConfiguration.add(new PackageMaterialProperty(AUTH_TOKEN).with(PackageMaterialProperty.DISPLAY_NAME, "Authentication token").with(PackageMaterialProperty.DISPLAY_ORDER, 1).with(PackageMaterialProperty.SECURE, true));        
        repositoryConfiguration.add(new PackageMaterialProperty(PROJECT_NAME).with(PackageMaterialProperty.DISPLAY_NAME, "Project name").with(PackageMaterialProperty.DISPLAY_ORDER, 2));
        return repositoryConfiguration;
	}

	@Override
	public ValidationResult isPackageConfigurationValid(
			PackageConfiguration arg0, RepositoryConfiguration arg1) {
		return new ValidationResult();
	}

	@Override
	public ValidationResult isRepositoryConfigurationValid(
			RepositoryConfiguration repositoryConfiguration) {
		ValidationResult validationResult = new ValidationResult();
        Property url = repositoryConfiguration.get("URL");
        Property token = repositoryConfiguration.get("TOKEN");
        Property project = repositoryConfiguration.get("PROJECT");
        if (url.getValue() != null && !"".equals(url.getValue())) {
            validationResult.addError(new ValidationError("URL cannot be empty"));
        } else if (token.getValue() != null && !"".equals(token.getValue())) {
            validationResult.addError(new ValidationError("Token cannot be empty"));
        } else if (project.getValue() != null && !"".equals(project.getValue())) {
            validationResult.addError(new ValidationError("Project name cannot be empty"));
        }
        return validationResult;
	}

}
