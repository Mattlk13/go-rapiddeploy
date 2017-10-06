package com.midvision.go.plugin.packagematerial;

import static com.midvision.go.plugin.packagematerial.message.CheckConnectionResultMessage.STATUS.FAILURE;
import static com.midvision.go.plugin.packagematerial.message.CheckConnectionResultMessage.STATUS.SUCCESS;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.midvision.go.plugin.packagematerial.message.CheckConnectionResultMessage;
import com.midvision.go.plugin.packagematerial.message.PackageMaterialProperties;
import com.midvision.go.plugin.packagematerial.message.PackageRevisionMessage;
import com.midvision.go.plugin.packagematerial.message.ValidationResultMessage;
import com.midvision.go.plugin.packagematerial.utils.NaturalOrderComparator;
import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.logging.Logger;

public class RapidDeployPackageMaterialPoller {

	private static final Logger LOGGER = Logger.getLoggerFor(RapidDeployPackageMaterialPoller.class);

	private final RapidDeployRepositoryConfigurationProvider configurationProvider;

	public RapidDeployPackageMaterialPoller(final RapidDeployRepositoryConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	public CheckConnectionResultMessage checkConnectionToRepository(final PackageMaterialProperties repositoryConfiguration) {
		final ValidationResultMessage validationResultMessage = configurationProvider.validateRepositoryConfiguration(repositoryConfiguration);
		if (validationResultMessage.failure()) {
			return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, validationResultMessage.getMessages());
		}
		final String serverUrl = repositoryConfiguration.getProperty(RapidDeployConstants.SERVER_URL).value();
		try {
			// Check connection by getting the package list
			getProjectPackages(repositoryConfiguration);
			return new CheckConnectionResultMessage(SUCCESS, asList(String.format("Successfully connected to RapidDeploy at %s", serverUrl)));
		} catch (final Exception e) {
			LOGGER.warn(String.format("[RapidDeploy Check Connection] Check connection for %s failed with exception - %s", serverUrl, e));
			return new CheckConnectionResultMessage(FAILURE, asList(String.format("Could not connect to RapidDeploy at %s", serverUrl, e.getMessage())));
		}
	}

	public CheckConnectionResultMessage checkConnectionToPackage(final PackageMaterialProperties packageConfiguration,
			final PackageMaterialProperties repositoryConfiguration) {
		final CheckConnectionResultMessage result = checkConnectionToRepository(repositoryConfiguration);
		if (!result.success()) {
			return result;
		}
		try {
			final ValidationResultMessage validationResultMessage = configurationProvider.validatePackageConfiguration(packageConfiguration);
			if (validationResultMessage.failure()) {
				return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, validationResultMessage.getMessages());
			}
			final PackageRevisionMessage latestRevision = getLatestRevision(packageConfiguration, repositoryConfiguration);
			if (latestRevision == null || latestRevision.getRevision() == null || "".equals(latestRevision.getRevision())) {
				return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList("Could not find package in the repository!"));
			} else {
				return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.SUCCESS,
						asList(String.format("Found package '%s'.", latestRevision.getRevision())));
			}
		} catch (final Exception e) {
			final String message = String.format("Could not find any package that matched '%s'.",
					packageConfiguration.getProperty(RapidDeployConstants.PACKAGE_ID).value());
			LOGGER.warn(message);
			return new CheckConnectionResultMessage(CheckConnectionResultMessage.STATUS.FAILURE, asList(message));
		}
	}

	public PackageRevisionMessage getLatestRevision(final PackageMaterialProperties packageConfiguration,
			final PackageMaterialProperties repositoryConfiguration) {
		validateData(packageConfiguration, repositoryConfiguration);
		String packageName = null;
		try {
			packageName = getLatestPackageNameFromRepo(packageConfiguration, repositoryConfiguration);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
		return new PackageRevisionMessage(packageName, new Date(), null, null, null);
	}

	public PackageRevisionMessage getLatestRevisionSince(final PackageMaterialProperties packageConfiguration,
			final PackageMaterialProperties repositoryConfiguration, final PackageRevisionMessage previousPackageRevision) {
		final PackageRevisionMessage latestRevision = getLatestRevision(packageConfiguration, repositoryConfiguration);
		// check if package version is higher - compare
		if (new NaturalOrderComparator().compare(latestRevision.getRevision(), previousPackageRevision.getRevision()) > 0) {
			return new PackageRevisionMessage(latestRevision.getRevision(), new Date(), null, null, null);
		} else {
			return previousPackageRevision;
		}
	}

	/** AUXILIARY METHODS **/

	private List<String> getProjectPackages(final PackageMaterialProperties repositoryConfiguration) throws Exception {

		final String rapidDeployUrl = repositoryConfiguration.getProperty(RapidDeployConstants.SERVER_URL).value();
		final String authToken = repositoryConfiguration.getProperty(RapidDeployConstants.AUTH_TOKEN).value();
		final String projectName = repositoryConfiguration.getProperty(RapidDeployConstants.PROJECT_NAME).value();

		final List<String> packages = RapidDeployConnector.invokeRapidDeployListPackages(authToken, rapidDeployUrl, projectName);
		return packages;
	}

	private String getLatestPackageNameFromRepo(final PackageMaterialProperties packageConfiguration, final PackageMaterialProperties repositoryConfiguration)
			throws Exception {

		final String packageIdentifier = packageConfiguration.getProperty(RapidDeployConstants.PACKAGE_ID).value();

		final List<String> packages = getProjectPackages(repositoryConfiguration);
		List<String> filteredPackages = null;
		if (packages != null) {
			filteredPackages = new ArrayList<String>();
			for (final String packageName : packages) {
				if (packageName.contains(packageIdentifier)) {
					filteredPackages.add(packageName);
				}
			}
		}

		if (filteredPackages != null && filteredPackages.size() > 0) {
			Collections.sort(filteredPackages, new NaturalOrderComparator());
			return filteredPackages.get(filteredPackages.size() - 1);
		}

		return null;
	}

	private void validateData(final PackageMaterialProperties packageConfiguration, final PackageMaterialProperties repositoryConfiguration) {
		final ValidationResultMessage repoValidationResultMessage = configurationProvider.validateRepositoryConfiguration(repositoryConfiguration);
		final ValidationResultMessage pkgValidationResultMessage = configurationProvider.validatePackageConfiguration(packageConfiguration);
		if (repoValidationResultMessage.failure() || pkgValidationResultMessage.failure()) {
			final List<String> errors = new ArrayList<String>();
			errors.addAll(repoValidationResultMessage.getMessages());
			errors.addAll(pkgValidationResultMessage.getMessages());
			LOGGER.warn(String.format("Data validation failed!"));
			throw new RuntimeException("Data validation failed!");
		}
	}
}
