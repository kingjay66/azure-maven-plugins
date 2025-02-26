/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.appservice.utils;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.config.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.config.RuntimeConfig;
import com.microsoft.azure.toolkit.lib.appservice.model.JavaVersion;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.plan.AppServicePlan;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.microsoft.azure.toolkit.lib.common.utils.Utils.copyProperties;
import static com.microsoft.azure.toolkit.lib.common.utils.Utils.selectFirstOptionIfCurrentInvalid;

public class AppServiceConfigUtils {
    private static final String SETTING_DOCKER_IMAGE = "DOCKER_CUSTOM_IMAGE_NAME";
    private static final String SETTING_REGISTRY_SERVER = "DOCKER_REGISTRY_SERVER_URL";

    public static AppServiceConfig fromAppService(@Nonnull AppServiceAppBase<?, ?, ?> app, @Nonnull AppServicePlan servicePlan) {
        AppServiceConfig config = new AppServiceConfig();
        config.appName(app.getName());

        config.resourceGroup(app.getResourceGroupName());
        config.subscriptionId(app.getSubscriptionId());
        config.region(app.getRegion());
        final Runtime runtime = app.getRuntime();

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        if (runtime != null && runtime.isDocker()) {
            runtimeConfig.os(OperatingSystem.DOCKER);
            final Map<String, String> settings = app.getAppSettings();

            final String imageSetting = settings.get(SETTING_DOCKER_IMAGE);
            if (StringUtils.isNotBlank(imageSetting)) {
                runtimeConfig.image(imageSetting);
            } else {
                runtimeConfig.image(Utils.getDockerImageNameFromLinuxFxVersion(app.getLinuxFxVersion()));
            }
            final String registryServerSetting = settings.get(SETTING_REGISTRY_SERVER);
            if (StringUtils.isNotBlank(registryServerSetting)) {
                runtimeConfig.registryUrl(registryServerSetting);
            }
        } else {
            runtimeConfig.os(runtime.getOperatingSystem());
            runtimeConfig.webContainer(runtime.getWebContainer());
            runtimeConfig.javaVersion(runtime.getJavaVersion());
        }
        config.runtime(runtimeConfig);
        config.pricingTier(servicePlan.getPricingTier());
        config.servicePlanName(servicePlan.name());
        config.servicePlanResourceGroup(servicePlan.getResourceGroupName());
        return config;
    }

    public static AppServiceConfig buildDefaultWebAppConfig(String subscriptionId, String resourceGroup, String appName, String packaging, JavaVersion javaVersion) {
        final AppServiceConfig appServiceConfig = AppServiceConfig.buildDefaultWebAppConfig(resourceGroup, appName, packaging, javaVersion);
        final List<Region> regions = Azure.az(AzureAppService.class).forSubscription(subscriptionId).listSupportedRegions();
        // replace with first region when the default region is not present
        appServiceConfig.region(selectFirstOptionIfCurrentInvalid("region", regions, appServiceConfig.region()));
        return appServiceConfig;
    }

    public static AppServiceConfig buildDefaultFunctionConfig(String subscriptionId, String resourceGroup, String appName, JavaVersion javaVersion) {
        final AppServiceConfig appServiceConfig = AppServiceConfig.buildDefaultFunctionConfig(resourceGroup, appName, javaVersion);
        final List<Region> regions = Azure.az(AzureAppService.class).forSubscription(subscriptionId).listSupportedRegions();
        // replace with first region when the default region is not present
        appServiceConfig.region(selectFirstOptionIfCurrentInvalid("region", regions, appServiceConfig.region()));
        return appServiceConfig;
    }

    public static void mergeAppServiceConfig(AppServiceConfig to, AppServiceConfig from) {
        try {
            copyProperties(to, from, true);
        } catch (IllegalAccessException e) {
            throw new AzureToolkitRuntimeException("Cannot copy object for class AppServiceConfig.", e);
        }

        if (to.runtime() != from.runtime()) {
            mergeRuntime(to.runtime(), from.runtime());
        }
    }

    private static void mergeRuntime(RuntimeConfig to, RuntimeConfig from) {
        try {
            copyProperties(to, from, true);
        } catch (IllegalAccessException e) {
            throw new AzureToolkitRuntimeException("Cannot copy object for class RuntimeConfig.", e);
        }
    }
}
