/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.thoughtworks.go.task.rpmbuild;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Extension
public class RPMBuildTask implements Task {

    static final String BUILD_ROOT = "BUILD_ROOT";
    static final String RPM_CONTROL_DIRECTORY = "RPM_CONTROL_DIRECTORY";
    static final String PACKAGE_NAME = "PACKAGE_NAME";

    @Override
    public TaskConfig config() {
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addProperty(BUILD_ROOT).withDefault(".");
        taskConfig.addProperty(RPM_CONTROL_DIRECTORY).withDefault(".");
        taskConfig.addProperty(PACKAGE_NAME).withDefault("default-package-name");
        return taskConfig;
    }

    @Override
    public TaskExecutor executor() {
        return new TaskExecutor() {
            @Override
            public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
                String rpmControlDirectory = taskConfig.getValue(RPM_CONTROL_DIRECTORY);
                String buildRoot = taskConfig.getValue(BUILD_ROOT);
                String packageName = taskConfig.getValue(PACKAGE_NAME);
                List<String> command = Arrays.asList("rpmbuild", "--buildroot", buildRoot, "--define", String.format("'_rpmdir %s'", rpmControlDirectory), "-bb",
                        "--target", "noarch", String.format("%s%s%s.spec", rpmControlDirectory, File.separator, packageName));
                try {
                    taskExecutionContext.console().printLine("[exec] " + StringUtils.join(command, " "));
                    ProcessBuilder builder = new ProcessBuilder(command).directory(new File(taskExecutionContext.workingDir()));
                    Process process = builder.start();
                    taskExecutionContext.console().readOutputOf(process.getInputStream());
                    taskExecutionContext.console().readErrorOf(process.getErrorStream());
                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        // continue
                    }
                    int exitValue = process.exitValue();
                    if (exitValue != 0) {
                        return ExecutionResult.failure("[exec] FAILED with return code " + exitValue);
                    }
                } catch (IOException e) {
                    taskExecutionContext.console().printLine("[exec] EXCEPTION with message " + e.getMessage());
                    throw new RuntimeException(e);
                }
                return ExecutionResult.success(String.format("[exec] Successfully executed command [%s]", StringUtils.join(command, " ")));
            }
        };
    }

    @Override
    public TaskView view() {
        return new TaskView() {
            @Override
            public String displayValue() {
                return "RPM Build";
            }

            @Override
            public String template() {
                return "<div class=\"form_item_block\">\n" +
                        "    <label>Build Root:<span class=\"asterisk\">*</span></label>\n" +
                        "    <input type=\"text\" ng-model=\"BUILD_ROOT\" ng-required=\"true\" />\n" +
                        "</div>\n" +
                        "<div class=\"form_item_block\">\n" +
                        "    <label>RPM Control Directory:<span class=\"asterisk\">*</span></label>\n" +
                        "    <input type=\"text\" ng-model=\"RPM_CONTROL_DIRECTORY\" ng-required=\"true\" />\n" +
                        "</div>\n" +
                        "<div class=\"form_item_block\">\n" +
                        "    <label>Package Name (without extension):<span class=\"asterisk\">*</span></label>\n" +
                        "    <input type=\"text\" ng-model=\"PACKAGE_NAME\" ng-required=\"true\" />\n" +
                        "</div>\n";
            }
        };
    }

    @Override
    public ValidationResult validate(TaskConfig taskConfig) {
        return new ValidationResult();
    }
}
