/*
 * Copyright (c) 2023 Risu
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 *
 */

package io.github.risu729.hammuni;

import com.google.common.util.concurrent.MoreExecutors;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
public class SchedulerConfiguration {

  @Bean
  public @NotNull ScheduledExecutorService scheduler() {
    var threadPoolExecutor = new ScheduledThreadPoolExecutor(3);
    threadPoolExecutor.setRemoveOnCancelPolicy(true);
    return MoreExecutors.getExitingScheduledExecutorService(threadPoolExecutor, Duration.ZERO);
  }
}
