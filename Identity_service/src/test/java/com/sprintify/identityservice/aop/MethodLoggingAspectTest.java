package com.sprintify.identityservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MethodLoggingAspectTest {

    @InjectMocks
    private MethodLoggingAspect methodLoggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("UserService.getProfile(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
    }

    @Test
    void logMethodCall_proceedsWithJoinPoint() throws Throwable {
        when(joinPoint.proceed()).thenReturn("result");

        Object result = methodLoggingAspect.logMethodCall(joinPoint);

        verify(joinPoint).proceed();
        assertThat(result).isEqualTo("result");
    }

    @Test
    void logMethodCall_returnsResultFromProceed() throws Throwable {
        Object expected = new Object();
        when(joinPoint.proceed()).thenReturn(expected);

        Object actual = methodLoggingAspect.logMethodCall(joinPoint);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void logMethodCall_returnsNullWhenProceedReturnsNull() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        Object result = methodLoggingAspect.logMethodCall(joinPoint);

        assertThat(result).isNull();
    }

    @Test
    void logMethodCall_propagatesExceptionThrownByJoinPoint() throws Throwable {
        RuntimeException runtimeException = new RuntimeException("Service failed");
        when(joinPoint.proceed()).thenThrow(runtimeException);

        assertThatThrownBy(() -> methodLoggingAspect.logMethodCall(joinPoint))
                .isSameAs(runtimeException);
    }

    @Test
    void logMethodCall_logsEvenWhenExceptionIsThrown() throws Throwable {
        // Verify that the finally block runs (logging happens) even on exception.
        // The aspect should call getSignature() and getArgs() regardless.
        when(joinPoint.proceed()).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> methodLoggingAspect.logMethodCall(joinPoint));

        // Verify that signature and args were accessed (logging took place)
        verify(joinPoint).getSignature();
        verify(joinPoint).getArgs();
    }

    @Test
    void logMethodCall_worksWithNoArguments() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = methodLoggingAspect.logMethodCall(joinPoint);

        assertThat(result).isEqualTo("ok");
    }

    @Test
    void logMethodCall_worksWithNullReturnValue() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        // Should not throw
        Object result = methodLoggingAspect.logMethodCall(joinPoint);

        assertThat(result).isNull();
        verify(joinPoint).proceed();
    }

    @Test
    void logMethodCall_accessesMethodNameAndArgs() throws Throwable {
        when(joinPoint.proceed()).thenReturn("done");

        methodLoggingAspect.logMethodCall(joinPoint);

        verify(signature).toShortString();
        verify(joinPoint).getArgs();
    }
}