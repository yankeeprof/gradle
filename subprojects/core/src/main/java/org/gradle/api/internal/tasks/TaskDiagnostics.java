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

package org.gradle.api.internal.tasks;

import org.gradle.api.Task;
import org.gradle.api.internal.GeneratedSubclasses;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.internal.tasks.properties.InputFilePropertyType;
import org.gradle.api.internal.tasks.properties.OutputFilePropertyType;
import org.gradle.api.internal.tasks.properties.PropertyValue;
import org.gradle.api.internal.tasks.properties.PropertyVisitor;
import org.gradle.api.internal.tasks.properties.PropertyWalker;
import org.gradle.api.tasks.FileNormalizer;
import org.gradle.internal.fingerprint.DirectorySensitivity;
import org.gradle.internal.logging.text.TreeFormatter;
import org.gradle.internal.reflect.TypeValidationContext;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

public class TaskDiagnostics {
    private final PropertyWalker propertyWalker;
    private final FileCollectionFactory fileCollectionFactory;

    public TaskDiagnostics(GradleInternal gradleInternal) {
        propertyWalker = gradleInternal.getServices().get(PropertyWalker.class);
        fileCollectionFactory = gradleInternal.getServices().get(FileCollectionFactory.class);
    }

    public void reportTaskPropertiesForTaskGraph(TaskInternal task, Set<Task> dependencies) {
        taskHeader(task);
        System.out.println("dependencies:");
        for (Task dependency : dependencies) {
            System.out.println(((TaskInternal) dependency).getIdentityPath());
        }
        reportTaskProperties(task, false);
    }

    public void reportTaskPropertiesForExecution(TaskInternal task) {
        taskHeader(task);
        reportTaskProperties(task, true);
    }

    private void reportTaskProperties(TaskInternal task, boolean showFileCollectionContents) {
        propertyWalker.visitProperties(task, TypeValidationContext.NOOP, new PropertyVisitor.Adapter() {
            @Override
            public void visitInputFileProperty(String propertyName, boolean optional, boolean skipWhenEmpty, DirectorySensitivity directorySensitivity, boolean incremental, @Nullable Class<? extends FileNormalizer> fileNormalizer, PropertyValue value, InputFilePropertyType filePropertyType) {
                reportProperty("Input file", propertyName, value);
            }

            @Override
            public void visitOutputFileProperty(String propertyName, boolean optional, PropertyValue value, OutputFilePropertyType filePropertyType) {
                reportProperty("Output file", propertyName, value);
            }

            private void reportProperty(String type, String propertyName, PropertyValue propertyValue) {
                System.out.println();
                System.out.println(type + " property " + task.getIdentityPath() + ':' + propertyName);
                System.out.println("spec:");
                Object value = propertyValue.getUnprocessedValue();
                TreeFormatter formatter = new TreeFormatter();
                describeTo(value, formatter);
                System.out.println(formatter.toString());
                if (showFileCollectionContents && propertyValue.call() != null) {
                    System.out.println("contents:");
                    for (File file : fileCollectionFactory.resolving(propertyValue)) {
                        System.out.println(file);
                    }
                }
            }
        });
    }

    private void taskHeader(TaskInternal task) {
        System.out.println();
        System.out.println("Task " + task.getIdentityPath() + " with type " + GeneratedSubclasses.unpackType(task));
    }

    private static void describeTo(@Nullable Object value, TreeFormatter formatter) {
        if (value == null) {
            formatter.node("null");
        } else if (value instanceof FileCollectionInternal) {
            FileCollectionInternal files = (FileCollectionInternal) value;
            files.describeContents(formatter);
        } else {
            formatter.node(value + " (class: " + value.getClass().getName() + ")");
        }
    }
}
