package com.github.noconnor.junitperf.statements;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class DefaultStatementTest extends TestCase {

    @Mock
    private Statement statementMock;

    @Test
    public void whenRunningDefaultStatement_thenUnderlyingStatementShouldBeCalled() throws Throwable {
        DefaultStatement defaultStatement = new DefaultStatement(statementMock);
        defaultStatement.evaluate();
        verify(statementMock).evaluate();
    }

    @Test
    public void whenRunningDefaultStatementBefores_thenNothingShouldHappen() {
        DefaultStatement defaultStatement = new DefaultStatement(statementMock);
        defaultStatement.runBefores();
        verifyNoInteractions(statementMock);
    }

    @Test
    public void whenRunningDefaultStatementAfters_thenNothingShouldHappen() {
        DefaultStatement defaultStatement = new DefaultStatement(statementMock);
        defaultStatement.runAfters();
        verifyNoInteractions(statementMock);
    }
}