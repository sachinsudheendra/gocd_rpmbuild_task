package com.thoughtworks.go.task.rpmbuild;

import com.thoughtworks.go.plugin.api.response.execution.ExecutionResult;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import com.thoughtworks.go.plugin.api.task.Console;
import com.thoughtworks.go.plugin.api.task.TaskConfig;
import com.thoughtworks.go.plugin.api.task.TaskExecutionContext;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RPMBuildTaskTest {

    private RPMBuildTask task;

    @Before
    public void setUp() throws Exception {
        task = new RPMBuildTask();
    }

    @Test
    public void shouldAddDefaultConfigurationToTaskConfig() throws Exception {
        TaskConfig config = task.config();
        assertThat(config.size(), is(2));
        assertThat(config.getValue(RPMBuildTask.TARGET_ARCH), is("noarch"));
        assertThat(config.getValue(RPMBuildTask.SPEC_FILE), is("package.spec"));
    }

    @Test
    public void shouldExecuteRPMBuild() throws Exception {
        TaskExecutionContext executor = mock(TaskExecutionContext.class);
        Console console = mock(Console.class);
        doNothing().when(console).printLine(anyString());
        doNothing().when(console).readErrorOf(any(InputStream.class));
        doNothing().when(console).readOutputOf(any(InputStream.class));
        when(executor.console()).thenReturn(console);
        RPMBuildTask spy = spy(task);
        Process process = mock(Process.class);
        when(process.waitFor()).thenReturn(0);
        when(process.exitValue()).thenReturn(0);
        doReturn(process).when(spy).runProcess(eq(executor), anyList());

        ExecutionResult result = spy.executor().execute(task.config(), executor);

        assertThat(result.isSuccessful(), is(true));
        assertThat(result.getMessagesForDisplay(), is("[exec] Successfully executed command [rpmbuild --target noarch -bb -v --clean package.spec]"));
        verify(process).waitFor();
        verify(process).exitValue();
        verify(process).getInputStream();
        verify(process).getErrorStream();
        verify(executor, times(3)).console();
        verify(spy).runProcess(eq(executor), anyList());
    }

    @Test
    public void shouldFailWhenExitValueIsNonZero() throws Exception {
        TaskExecutionContext executor = mock(TaskExecutionContext.class);
        Console console = mock(Console.class);
        doNothing().when(console).printLine(anyString());
        doNothing().when(console).readErrorOf(any(InputStream.class));
        doNothing().when(console).readOutputOf(any(InputStream.class));
        when(executor.console()).thenReturn(console);
        RPMBuildTask spy = spy(task);
        Process process = mock(Process.class);
        when(process.waitFor()).thenReturn(0);
        when(process.exitValue()).thenReturn(1);
        doReturn(process).when(spy).runProcess(eq(executor), anyList());

        ExecutionResult result = spy.executor().execute(task.config(), executor);

        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("[exec] FAILED with return code 1"));
        verify(process).waitFor();
        verify(process).exitValue();
        verify(process).getInputStream();
        verify(process).getErrorStream();
        verify(executor, times(3)).console();
        verify(spy).runProcess(eq(executor), anyList());
    }

    @Test
    public void shouldFailWhenExceptionIsThrown() throws Exception {
        TaskExecutionContext executor = mock(TaskExecutionContext.class);
        Console console = mock(Console.class);
        doNothing().when(console).printLine(anyString());
        doNothing().when(console).readErrorOf(any(InputStream.class));
        doNothing().when(console).readOutputOf(any(InputStream.class));
        when(executor.console()).thenReturn(console);
        RPMBuildTask spy = spy(task);
        doThrow(new IOException("foo")).when(spy).runProcess(eq(executor), anyList());

        ExecutionResult result = spy.executor().execute(task.config(), executor);

        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessagesForDisplay(), is("[exec] Exception: foo"));
        verify(executor).console();
        verify(spy).runProcess(eq(executor), anyList());
    }

    @Test
    public void shouldSetDisplayValue() throws Exception {
        assertThat(task.view().displayValue(), is("RPM Build"));
    }

    @Test
    public void shouldReturnEmptyValidation() throws Exception {
        assertThat(task.validate(task.config()).isSuccessful(), is(true));
    }
}