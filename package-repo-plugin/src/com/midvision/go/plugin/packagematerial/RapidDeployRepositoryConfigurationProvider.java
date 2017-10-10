package com.midvision.go.plugin.packagematerial;

import java.util.ArrayList;
import java.util.List;

import com.midvision.go.plugin.packagematerial.message.PackageMaterialProperties;
import com.midvision.go.plugin.packagematerial.message.PackageMaterialProperty;
import com.midvision.go.plugin.packagematerial.message.ValidationError;
import com.midvision.go.plugin.packagematerial.message.ValidationResultMessage;

public class RapidDeployRepositoryConfigurationProvider {

	public PackageMaterialProperties repositoryConfiguration() {
		final PackageMaterialProperties repositoryConfigurationResponse = new PackageMaterialProperties();
		repositoryConfigurationResponse.addPackageMaterialProperty(RapidDeployConstants.SERVER_URL, serverUrl());
		repositoryConfigurationResponse.addPackageMaterialProperty(RapidDeployConstants.AUTH_TOKEN, authToken());
		repositoryConfigurationResponse.addPackageMaterialProperty(RapidDeployConstants.PROJECT_NAME, projectName());
		return repositoryConfigurationResponse;
	}

	public PackageMaterialProperties packageConfiguration() {
		final PackageMaterialProperties packageConfigurationResponse = new PackageMaterialProperties();
		packageConfigurationResponse.addPackageMaterialProperty(RapidDeployConstants.PACKAGE_ID, packageId());
		return packageConfigurationResponse;
	}

	public ValidationResultMessage validateRepositoryConfiguration(final PackageMaterialProperties configurationProvidedByUser) {
		final ValidationResultMessage validationResultMessage = new ValidationResultMessage();
		validateKeys(repositoryConfiguration(), configurationProvidedByUser, validationResultMessage);
		final PackageMaterialProperty serverUrl = configurationProvidedByUser.getProperty(RapidDeployConstants.SERVER_URL);
		final PackageMaterialProperty authToken = configurationProvidedByUser.getProperty(RapidDeployConstants.AUTH_TOKEN);
		final PackageMaterialProperty projectName = configurationProvidedByUser.getProperty(RapidDeployConstants.PROJECT_NAME);
		if (serverUrl == null || serverUrl.value() == null || "".equals(serverUrl)) {
			validationResultMessage.addError(ValidationError.create(RapidDeployConstants.SERVER_URL, "The server URL can not be empty."));
		} else if (authToken == null || authToken.value() == null || "".equals(authToken)) {
			validationResultMessage.addError(ValidationError.create(RapidDeployConstants.SERVER_URL, "The authentication token can not be empty."));
		} else if (projectName == null || projectName.value() == null || "".equals(projectName)) {
			validationResultMessage.addError(ValidationError.create(RapidDeployConstants.SERVER_URL, "The project name can not be empty."));
		}
		return validationResultMessage;
	}

	public ValidationResultMessage validatePackageConfiguration(final PackageMaterialProperties configurationProvidedByUser) {
		final ValidationResultMessage validationResultMessage = new ValidationResultMessage();
		validateKeys(packageConfiguration(), configurationProvidedByUser, validationResultMessage);
		final PackageMaterialProperty packageId = configurationProvidedByUser.getProperty(RapidDeployConstants.PACKAGE_ID);
		if (packageId == null || packageId.value() == null || "".equals(packageId)) {
			validationResultMessage.addError(ValidationError.create(RapidDeployConstants.SERVER_URL, "The package ID can not be empty."));
		}
		return validationResultMessage;
	}

	/** AUXILIARY METHODS **/

	private void validateKeys(final PackageMaterialProperties configDefinedByPlugin, final PackageMaterialProperties configProvidedByUser,
			final ValidationResultMessage validationResultMessage) {
		final List<String> invalidKeys = new ArrayList<String>();
		for (final String key : configProvidedByUser.keys()) {
			if (!configDefinedByPlugin.hasKey(key)) {
				invalidKeys.add(key);
			}
		}
		if (!invalidKeys.isEmpty()) {
			validationResultMessage.addError(ValidationError.create(String.format("Unsupported key(s) found!")));
		}
	}

	/** PACKAGE MATERIAL PROPERTIES METHODS **/

	private PackageMaterialProperty serverUrl() {
		return new PackageMaterialProperty().withDisplayName("RapidDeploy URL").withDisplayOrder("0");
	}

	private PackageMaterialProperty authToken() {
		return new PackageMaterialProperty().withDisplayName("Authentication token").withDisplayOrder("1").withSecure(true);
	}

	private PackageMaterialProperty projectName() {
		return new PackageMaterialProperty().withDisplayName("Project name").withDisplayOrder("2");
	}

	private PackageMaterialProperty packageId() {
		return new PackageMaterialProperty().withDisplayName("Package identifier").withDisplayOrder("0");
	}
}
