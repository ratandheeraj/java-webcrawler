package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Objects;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with
 * the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method
 * invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState profilingState;

  // TODO: You will need to add more instance fields and constructor arguments to
  // this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState profilingState) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.profilingState = profilingState;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // TODO: This method interceptor should inspect the called method to see if it
    // is a profiled
    // method. For profiled methods, the interceptor should record the start time,
    // then
    // invoke the method using the object that is being profiled. Finally, for
    // profiled
    // methods, the interceptor should record how long the method call took, using
    // the
    // ProfilingState methods.
    Object invoked;
    Instant start = null;
    if (method.getAnnotation(Profiled.class) != null) {
      start = clock.instant();
    }
    try {
      invoked = method.invoke(delegate, args);
    } catch (IllegalAccessException ex) {
      throw new RuntimeException(ex);
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    }  finally {
      if (method.getAnnotation(Profiled.class) != null) {
        Duration duration = Duration.between(start, clock.instant());
        profilingState.record(delegate.getClass(), method, duration);
      }
    }

    return invoked;
  }
}
