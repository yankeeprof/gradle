/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.plugin.use.resolve.internal;

import org.gradle.internal.build.BuildState;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.build.IncludedBuildState;
import org.gradle.plugin.management.internal.InvalidPluginRequestException;
import org.gradle.plugin.management.internal.PluginRequestInternal;
import org.gradle.plugin.use.PluginId;
import org.gradle.plugin.use.internal.DefaultPluginId;
import org.gradle.util.Path;

public class BuildSrcPluginResolver implements PluginResolver {
    public static final PluginId BUILDSRC_INTERNAL = DefaultPluginId.of("internal.buildSrc");
    private final BuildStateRegistry buildStateRegistry;
    private final BuildState consumingBuild;

    public BuildSrcPluginResolver(BuildStateRegistry buildStateRegistry, BuildState consumingBuild) {
        this.buildStateRegistry = buildStateRegistry;
        this.consumingBuild = consumingBuild;
    }

    @Override
    public void resolve(PluginRequestInternal pluginRequest, PluginResolutionResult result) throws InvalidPluginRequestException {
        if (pluginRequest.getId().equals(BUILDSRC_INTERNAL)) {
            result.found("buildSrc", new PluginResolution() {
                @Override
                public PluginId getPluginId() {
                    return BUILDSRC_INTERNAL;
                }

                @Override
                public void execute(PluginResolveContext pluginResolveContext) {
                    Path buildSrcPath = consumingBuild.getIdentityPath().child("buildSrc");
                    for (IncludedBuildState includedBuildState : buildStateRegistry.getIncludedBuilds()) {
                        if (includedBuildState.getIdentityPath().equals(buildSrcPath)) {
                            pluginResolveContext.addLegacyBuildSrc(includedBuildState.getConfiguredBuild().getRootProject());
                        }
                    }
                }
            });
        }
    }
}
