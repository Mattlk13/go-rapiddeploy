package com.tw.go.plugin.packagematerial;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;

@Extension
public class RapidDeployPackageRepoProvider implements PackageMaterialProvider{

	@Override
	public PackageMaterialConfiguration getConfig() {
		return new RapidDeployRepositoryConfiguration();
	}

	@Override
	public PackageMaterialPoller getPoller() {
		return new RapidDeployPackageMaterialPoller();
	}

}
