package com.tw.go.plugin.packagematerial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.midvision.rapiddeploy.connector.RapidDeployConnector;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.tw.go.plugin.packagematerial.utils.NaturalOrderComparator;

public class RapidDeployPackageMaterialPoller implements PackageMaterialPoller{

	@Override
	public Result checkConnectionToPackage(PackageConfiguration packageConfiguration,
			RepositoryConfiguration repositoryConfiguration) {
		
		Result result = new Result();
		String packageName = null;				
				
		try {
			packageName = getLatestPackageNameFromRepo(packageConfiguration, repositoryConfiguration);
			if(packageName == null || "".equals(packageName)){
				result.withErrorMessages("Could not find package in the repository!");
			} else{
				result.withSuccessMessages("Found package: " + packageName);
			}
		} catch (Exception e) {
			result.withErrorMessages("Error while retrieving the package!\nDetails: " + e.getMessage());
			e.printStackTrace();
		}
				
		return result;
	}

	@Override
	public Result checkConnectionToRepository(RepositoryConfiguration repositoryConfiguration) {
		
		Result result = new Result();		
		
		try {
			//check connection by getting the package list
			getProjectPackages(repositoryConfiguration);
			result.withSuccessMessages("Successfully connected to repository!");
		} catch (Exception e) {
			result.withErrorMessages("Error while connecting to the repository!\nDetails: " + e.getMessage());
			e.printStackTrace();
		}
				
		return result;
	}

	@Override
	public PackageRevision getLatestRevision(PackageConfiguration packageConfiguration,
			RepositoryConfiguration repositoryConfiguration) {
		
		String packageName = null;
		
		try {
			packageName = getLatestPackageNameFromRepo(packageConfiguration, repositoryConfiguration);
		} catch (Exception e) {			
			e.printStackTrace();
			return null;
		}
		
		return new PackageRevision(packageName, new Date(), "");
	}

	@Override
	public PackageRevision latestModificationSince(PackageConfiguration packageConfiguration,
			RepositoryConfiguration repositoryConfiguration, PackageRevision packageRevision) {
		
		String latestPackageName = null;
		
		try {
			latestPackageName = getLatestPackageNameFromRepo(packageConfiguration, repositoryConfiguration);
			String originalPackageName = packageRevision.getRevision();
			//check if package version is higher - compare
			if(new NaturalOrderComparator().compare(latestPackageName, originalPackageName)>0){
				return new PackageRevision(latestPackageName, new Date(), "");
			} else{
				return packageRevision;
			}
		} catch (Exception e) {			
			e.printStackTrace();
			return null;
		}	
	}
	
	private List<String> getProjectPackages(RepositoryConfiguration repositoryConfiguration) throws Exception{
		
		final String rapidDeployUrl = repositoryConfiguration.get(RapidDeployRepositoryConfiguration.SERVER_URL).getValue();
		final String authToken = repositoryConfiguration.get(RapidDeployRepositoryConfiguration.AUTH_TOKEN).getValue();
		final String projectName = repositoryConfiguration.get(RapidDeployRepositoryConfiguration.PROJECT_NAME).getValue();		
		
		List<String> packages = RapidDeployConnector.invokeRapidDeployListPackages(authToken, rapidDeployUrl, projectName);
		return packages;
	}
	
	private String getLatestPackageNameFromRepo(PackageConfiguration packageConfiguration,
			RepositoryConfiguration repositoryConfiguration) throws Exception{
		
		final String packageIdentifier = packageConfiguration.get(RapidDeployRepositoryConfiguration.PACKAGE_ID).getValue();
		
		List<String> packages = getProjectPackages(repositoryConfiguration);		
		List<String> filteredPackages = null;
		if(packages != null){
			filteredPackages = new ArrayList<String>();
			for(String packageName : packages){
				if(packageName.contains(packageIdentifier)){
					filteredPackages.add(packageName);
				}
			}						
		}
		
		if(filteredPackages != null && filteredPackages.size()>0){
			Collections.sort(filteredPackages, new NaturalOrderComparator());
			return filteredPackages.get(filteredPackages.size()-1);
		}
		
		return null;
	}
}
