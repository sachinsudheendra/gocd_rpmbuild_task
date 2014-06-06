package com.thoughtworks.go.task.rpmbuild;

import com.thoughtworks.go.plugin.api.task.TaskConfig;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RPMBuildTaskTest {

    @Test
    public void shouldAddDefaultConfigurationToTaskConfig() throws Exception {
        RPMBuildTask task = new RPMBuildTask();
        TaskConfig config = task.config();
        assertThat(config.size(), is(2));
        assertThat(config.getValue(RPMBuildTask.TARGET_ARCH), is("noarch"));
        assertThat(config.getValue(RPMBuildTask.SPEC_FILE), is("package.spec"));
    }
}