/*
 * Copyright 2007-2008 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.component.ModuleComponentSelector;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.internal.component.external.model.DefaultModuleComponentSelector;
import org.gradle.internal.component.model.Exclude;
import org.gradle.internal.component.model.IvyArtifactName;
import org.gradle.internal.component.model.LocalComponentDependencyMetadata;
import org.gradle.internal.component.model.LocalOriginDependencyMetadata;
import org.gradle.util.WrapUtil;

import java.util.Collections;
import java.util.List;

public class DefaultDependencyDescriptorFactory implements DependencyDescriptorFactory {
    private List<IvyDependencyDescriptorFactory> dependencyDescriptorFactories;

    public DefaultDependencyDescriptorFactory(IvyDependencyDescriptorFactory... dependencyDescriptorFactories) {
        this.dependencyDescriptorFactories = WrapUtil.toList(dependencyDescriptorFactories);
    }

    public LocalOriginDependencyMetadata createDependencyDescriptor(String clientConfiguration, AttributeContainer attributes, ModuleDependency dependency) {
        IvyDependencyDescriptorFactory factoryInternal = findFactoryForDependency(dependency);
        return factoryInternal.createDependencyDescriptor(clientConfiguration, attributes, dependency);
    }

    public LocalOriginDependencyMetadata createDependencyConstraintDescriptor(String clientConfiguration, AttributeContainer attributes, DependencyConstraint dependencyConstraint) {
        ModuleComponentSelector selector = DefaultModuleComponentSelector.newSelector(
            nullToEmpty(dependencyConstraint.getGroup()), nullToEmpty(dependencyConstraint.getName()), dependencyConstraint.getVersionConstraint());
        return new LocalComponentDependencyMetadata(selector, clientConfiguration, attributes, null,
            Collections.<IvyArtifactName>emptySet(), Collections.<Exclude>emptyList(), false, false, true, true);
    }

    private IvyDependencyDescriptorFactory findFactoryForDependency(ModuleDependency dependency) {
        for (IvyDependencyDescriptorFactory ivyDependencyDescriptorFactory : dependencyDescriptorFactories) {
            if (ivyDependencyDescriptorFactory.canConvert(dependency)) {
                return ivyDependencyDescriptorFactory;
            }
        }
        throw new InvalidUserDataException("Can't map dependency of type: " + dependency.getClass());
    }

    private String nullToEmpty(String input) {
        return input == null ? "" : input;
    }
}
