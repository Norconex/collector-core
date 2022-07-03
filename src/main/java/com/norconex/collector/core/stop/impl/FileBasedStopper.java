/* Copyright 2021-2022 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.collector.core.stop.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.Collector;
import com.norconex.collector.core.monitor.MdcUtil;
import com.norconex.collector.core.stop.CollectorStopperException;
import com.norconex.collector.core.stop.ICollectorStopper;
import com.norconex.commons.lang.Sleeper;

/**
 * Listens for STOP requests using a stop file.  The stop file
 * file is created under the working directory as
 * <code>.collector-stop</code>.
 *
 * @author Pascal Essiembre
 * @since 2.0.0
 */
public class FileBasedStopper implements ICollectorStopper {

    private static final Logger LOG =
            LoggerFactory.getLogger(FileBasedStopper.class);

    private Collector startedCollector;
    private boolean monitoring;

    @Override
    public void listenForStopRequest(Collector startedCollector)
            throws CollectorStopperException {
        this.startedCollector = startedCollector;
        final Path stopFile = stopFile(startedCollector);

        // If there is already a stop file and the collector is not running,
        // delete it
        if (stopFile.toFile().exists() && !startedCollector.isRunning()) {
            LOG.info("Old stop file found, deleting it.");
            try {
                FileUtils.forceDelete(stopFile.toFile());
            } catch (IOException e) {
                throw new CollectorStopperException(
                        "Could not delete old stop file.", e);
            }
        }

        ExecutorService execService = Executors.newSingleThreadExecutor();
        try {
            execService.submit(() -> {
                MdcUtil.setCollectorId(startedCollector.getId());
                Thread.currentThread().setName("Collector stop file monitor");
                monitoring = true;
                while(monitoring) {
                    if (stopFile.toFile().exists()) {
                        stopMonitoring(startedCollector);
                        LOG.info("STOP request received.");
                        startedCollector.stop();
                    }
                    Sleeper.sleepMillis(100);
                }
                return null;
            });
        } finally {
            execService.shutdownNow();
        }
    }

    @Override
    public void destroy() throws CollectorStopperException {
        if (startedCollector != null) {
            stopMonitoring(startedCollector);
        }
        startedCollector = null;
    }

    @Override
    public boolean fireStopRequest() throws CollectorStopperException {
        final Path stopFile = stopFile(startedCollector);

        if (!startedCollector.isRunning()) {
            LOG.info("CANNOT STOP: The Collector is not running.");
            return false;
        }

        if (stopFile.toFile().exists()) {
            LOG.info("CANNOT STOP: Stop already requested. Stop file: {}",
                    stopFile.toAbsolutePath());
            return false;
        }

        try {
            Files.createFile(stopFile);
        } catch (IOException e) {
            throw new CollectorStopperException("Could not create stop file: "
                    + stopFile.toAbsolutePath(), e);
        }
        return true;
    }

    private synchronized void stopMonitoring(Collector collector)
            throws CollectorStopperException {
        monitoring = false;
        Path stopFile = stopFile(collector);
        try {
            Files.deleteIfExists(stopFile);
        } catch (IOException e) {
            throw new CollectorStopperException(
                    "Cannot delete stop file: " + stopFile.toAbsolutePath(), e);
        }
    }
    private static Path stopFile(Collector collector) {
        return collector.getWorkDir().resolve(".collector-stop");
    }
}
