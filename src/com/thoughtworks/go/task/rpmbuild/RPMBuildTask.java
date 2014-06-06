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

    static final String TARGET_ARCH = "TARGET_ARCH";
    static final String SPEC_FILE = "SPEC_FILE";

    @Override
    public TaskConfig config() {
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.addProperty(TARGET_ARCH).withDefault("noarch");
        taskConfig.addProperty(SPEC_FILE).withDefault("package.spec");
        return taskConfig;
    }

    @Override
    public TaskExecutor executor() {
        return new TaskExecutor() {
            @Override
            public ExecutionResult execute(TaskConfig taskConfig, TaskExecutionContext taskExecutionContext) {
                String targetArch = taskConfig.getValue(TARGET_ARCH);
                String specFilePath = taskConfig.getValue(SPEC_FILE);
                List<String> command = Arrays.asList("rpmbuild", "--target", targetArch, "-bb", "-v", "--clean", specFilePath);
                try {
                    taskExecutionContext.console().printLine("[exec] " + StringUtils.join(command, " "));
                    Process process = runProcess(taskExecutionContext, command);
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
                    return ExecutionResult.failure("[exec] Exception: " + e.getMessage(), e);
                }
                return ExecutionResult.success(String.format("[exec] Successfully executed command [%s]", StringUtils.join(command, " ")));
            }
        };
    }

    Process runProcess(TaskExecutionContext taskExecutionContext, List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command).directory(new File(taskExecutionContext.workingDir()));
        return builder.start();
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
                        "    <label>Spec file (relative path from working directory):<span class=\"asterisk\">*</span></label>\n" +
                        "    <input type=\"text\" ng-model=\"SPEC_FILE\" ng-required=\"true\" />\n" +
                        "</div>\n" +
                        "<div class=\"form_item_block\">\n" +
                        "    <label>Target Arch:<span class=\"asterisk\">*</span></label>\n" +
                        "    <input type=\"text\" ng-model=\"TARGET_ARCH\" ng-required=\"true\" />\n" +
                        "</div>\n";
            }
        };
    }

    @Override
    public ValidationResult validate(TaskConfig taskConfig) {
        return new ValidationResult();
    }
}
