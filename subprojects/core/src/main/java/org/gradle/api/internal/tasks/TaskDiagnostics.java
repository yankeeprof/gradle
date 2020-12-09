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

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.UncheckedIOException;
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
import org.gradle.internal.file.FileMetadata;
import org.gradle.internal.file.Stat;
import org.gradle.internal.fingerprint.DirectorySensitivity;
import org.gradle.internal.logging.text.TreeFormatter;
import org.gradle.internal.reflect.TypeValidationContext;
import org.gradle.internal.scopeids.id.BuildInvocationScopeId;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class TaskDiagnostics {

    private final File logFile;

    private final PropertyWalker propertyWalker;
    private final FileCollectionFactory fileCollectionFactory;
    private final Stat stat;

    public TaskDiagnostics(GradleInternal gradleInternal) {
        logFile = resolveLogFile(gradleInternal);
        propertyWalker = gradleInternal.getServices().get(PropertyWalker.class);
        fileCollectionFactory = gradleInternal.getServices().get(FileCollectionFactory.class);
        stat = gradleInternal.getServices().get(Stat.class);
    }

    private File resolveLogFile(GradleInternal gradleInternal) {
        String buildInvocationId = gradleInternal.getServices().get(BuildInvocationScopeId.class).getId().asString();
        File rootDir = gradleInternal.getRoot().getRootProject().getProjectDir();
        return new File(rootDir, "task-diagnostics-" + buildInvocationId + ".log");
    }

    private synchronized void writingLogs(Action<PrintWriter> action) {
        try (FileOutputStream output = new FileOutputStream(logFile, true)) {
            action.execute(new PrintWriter(output));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void reportTaskGraph(List<Task> tasks, Function<Task, Set<Task>> taskDependenciesSupplier) {
        writingLogs(writer -> {
            writer.println("TASK GRAPH DETAILS");
            for (Task task : tasks) {
                TaskInternal taskInternal = (TaskInternal) task;
                reportTaskPropertiesForTaskGraph(writer, taskInternal, taskDependenciesSupplier.apply(task));
            }
        });
    }

    private void reportTaskPropertiesForTaskGraph(PrintWriter writer, TaskInternal task, Set<Task> dependencies) {
        taskHeader(writer, task);
        writer.println("dependencies:");
        for (Task dependency : dependencies) {
            writer.println(((TaskInternal) dependency).getIdentityPath());
        }
        reportTaskProperties(writer, task, false);
    }

    public void reportTaskPropertiesForExecution(TaskInternal task) {
        writingLogs(writer -> {
            taskHeader(writer, task);
            reportTaskProperties(writer, task, true);
        });
    }

    private void reportTaskProperties(PrintWriter writer, TaskInternal task, boolean showFileCollectionContents) {
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
                writer.println();
                writer.println(type + " property " + task.getIdentityPath() + ':' + propertyName);
                writer.println("spec:");
                Object value = propertyValue.getUnprocessedValue();
                TreeFormatter formatter = new TreeFormatter();
                describeTo(value, formatter);
                writer.println(formatter.toString());
                if (showFileCollectionContents && propertyValue.call() != null) {
                    writer.println("value:");
                    for (File file : fileCollectionFactory.resolving(propertyValue)) {
                        FileMetadata metadata = TaskDiagnostics.this.stat.stat(file);
                        writer.println(metadata.getType() + " " + file);
                    }
                }
            }
        });
    }

    private void taskHeader(PrintWriter writer, TaskInternal task) {
        writer.println();
        writer.println("=======");
        writer.println("Task " + task.getIdentityPath() + " with type " + GeneratedSubclasses.unpackType(task));
        writer.println("=======");
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
