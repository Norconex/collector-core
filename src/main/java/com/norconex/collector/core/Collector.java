/* Copyright 2014-2021 Norconex Inc.
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
package com.norconex.collector.core;

import static com.norconex.collector.core.CollectorEvent.COLLECTOR_CLEAN_BEGIN;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_CLEAN_END;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_RUN_BEGIN;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_RUN_END;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STORE_EXPORT_BEGIN;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STORE_EXPORT_END;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STORE_IMPORT_BEGIN;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STORE_IMPORT_END;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.crawler.Crawler;
import com.norconex.collector.core.crawler.CrawlerConfig;
import com.norconex.collector.core.stop.ICollectorStopper;
import com.norconex.collector.core.stop.impl.FileBasedStopper;
import com.norconex.committer.core3.ICommitter;
import com.norconex.commons.lang.ClassFinder;
import com.norconex.commons.lang.VersionUtil;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.file.FileAlreadyLockedException;
import com.norconex.commons.lang.file.FileLocker;
import com.norconex.commons.lang.file.FileUtil;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.importer.Importer;

/**
 * Base implementation of a Collector.
 * Instances of this class can hold several crawler, running at once.
 * This is convenient when there are configuration setting to be shared amongst
 * crawlers.  When you have many crawler jobs defined that have nothing
 * in common, it may be best to configure and run them separately, to facilitate
 * troubleshooting.  There is no best rule for this, experimentation
 * will help you.
 * @author Pascal Essiembre
 */
public abstract class Collector {

    /** Simple ASCI art of Norconex. */
    public static final String NORCONEX_ASCII =
            " _   _  ___  ____   ____ ___  _   _ _______  __\n"
          + "| \\ | |/ _ \\|  _ \\ / ___/ _ \\| \\ | | ____\\ \\/ /\n"
          + "|  \\| | | | | |_) | |  | | | |  \\| |  _|  \\  / \n"
          + "| |\\  | |_| |  _ <| |__| |_| | |\\  | |___ /  \\ \n"
          + "|_| \\_|\\___/|_| \\_\\\\____\\___/|_| \\_|_____/_/\\_\\\n\n"
          + "============== C O L L E C T O R ==============\n";

    private static final Logger LOG =
            LoggerFactory.getLogger(Collector.class);

    private static final InheritableThreadLocal<Collector> INSTANCE =
            new InheritableThreadLocal<>();

    private final CollectorConfig collectorConfig;
    private final List<Crawler> crawlers = new ArrayList<>();
    private final EventManager eventManager;

    private CachedStreamFactory streamFactory;
    private Path workDir;
    private Path tempDir;
    private FileLocker lock;

    //TODO make configurable
    private final ICollectorStopper stopper = new FileBasedStopper();

    /**
     * Creates and configure a Collector with the provided
     * configuration.
     * @param collectorConfig Collector configuration
     */
    public Collector(CollectorConfig collectorConfig) {
        this(collectorConfig, null);
    }

	/**
	 * Creates and configure a Collector with the provided
	 * configuration.
	 * @param collectorConfig Collector configuration
	 * @param eventManager event manager
	 */
    public Collector(
            CollectorConfig collectorConfig, EventManager eventManager) {

        //TODO clone config so modifications no longer apply?
        Objects.requireNonNull(
                collectorConfig, "'collectorConfig' must not be null.");

        this.collectorConfig = collectorConfig;
        this.eventManager = new EventManager(eventManager);

        INSTANCE.set(this);
    }

    public static Collector get() {
        return INSTANCE.get();
    }

    public synchronized Path getWorkDir() {
        if (workDir == null) {
            workDir = createCollectorSubDirectory(Optional.ofNullable(
                    collectorConfig.getWorkDir()).orElseGet(
                            () -> CollectorConfig.DEFAULT_WORK_DIR));
        }
        return workDir;
    }
    public synchronized Path getTempDir() {
        if (tempDir == null) {
            if (collectorConfig.getTempDir() == null) {
                tempDir = getWorkDir().resolve("temp");
            } else {
                tempDir = createCollectorSubDirectory(
                        collectorConfig.getTempDir());
            }
        }
        return tempDir;
    }

    private Path createCollectorSubDirectory(Path parentDir) {
        Objects.requireNonNull(parentDir, "'parentDir' must not be null.");
        String fileSafeId = FileUtil.toSafeFileName(getId());
        Path subDir = parentDir.resolve(fileSafeId);
        try {
            Files.createDirectories(subDir);
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create directory: " + subDir, e);
        }
        return subDir;
    }

    /**
     * Starts all crawlers defined in configuration.
     */
    public void start() {

        Thread.currentThread().setName(getId());

        // Version intro
        LOG.info("\n{}", getReleaseVersions());

        lock();
        try {
            initCollector();
            stopper.listenForStopRequest(this);
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_RUN_BEGIN, this).build());

            //TODO vvvvvvvvvvvvvvvvvvvvvv HANDLE Better
            //TODO replicate/migrate here what is necessary from JEF:
            //TODO     implement delayed start instead of threaded upon start
            //TODO     implement max threads for running crawlers

            getCrawlers().forEach(Crawler::start);

            //TODO ^^^^^^^^^^^^^^^^^^^^^^ HANDLE Better
        } finally {
            try {
                eventManager.fire(new CollectorEvent.Builder(
                        COLLECTOR_RUN_END, this).build());
                destroyCollector();
            } finally {
                stopper.destroy();
            }
        }
    }

    public void clean() {
        Thread.currentThread().setName(getId() + "/CLEAN");
        lock();
        try {
            initCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_CLEAN_BEGIN, this)
                        .message("Cleaning cached collector data (does not "
                               + "impact previously committed data)...")
                        .build());
            getCrawlers().forEach(Crawler::clean);
            destroyCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_CLEAN_END, this)
                        .message("Done cleaning collector.")
                        .build());
        } finally {
            eventManager.clearListeners();
            unlock();
        }
    }

    public void importDataStore(List<Path> inFiles) {
        Thread.currentThread().setName(getId() + "/IMPORT");
        lock();
        try {
            initCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_STORE_IMPORT_BEGIN, this).build());
            inFiles.forEach(f -> {
                getCrawlers().forEach(c -> c.importDataStore(f));
            });
            destroyCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_STORE_IMPORT_END, this).build());
        } finally {
            eventManager.clearListeners();
            unlock();
        }
    }
    public void exportDataStore(Path dir) {
        Thread.currentThread().setName(getId() + "/EXPORT");
        lock();
        try {
            initCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_STORE_EXPORT_BEGIN, this).build());
            //TODO zip all exported data stores in a single file?
            getCrawlers().forEach(c -> c.exportDataStore(dir));
            destroyCollector();
            eventManager.fire(new CollectorEvent.Builder(
                    COLLECTOR_STORE_EXPORT_END, this).build());
        } finally {
            eventManager.clearListeners();
            unlock();
        }
    }

    protected void initCollector() {

        // Ensure clean state
        tempDir = null;
        workDir = null;

        crawlers.clear();

        // recreate everything
        createCrawlers();

        //--- Register event listeners ---
        eventManager.addListenersFromScan(this.collectorConfig);

        //TODO move this code to a config validator class?
        //--- Ensure good state/config ---
        if (StringUtils.isBlank(collectorConfig.getId())) {
            throw new CollectorException("Collector must be given "
                    + "a unique identifier (id).");
        }

        //--- Stream Cache Factory ---
        streamFactory = new CachedStreamFactory(
                collectorConfig.getMaxMemoryPool(),
                collectorConfig.getMaxMemoryInstance(),
                getTempDir());
    }

    protected void destroyCollector() {
        try {
            FileUtil.delete(getTempDir().toFile());
        } catch (IOException e) {
            throw new CollectorException("Could not delete temp directory", e);
        } finally {
            eventManager.clearListeners();
            unlock();
        }
    }

    public void fireStopRequest() {
        stopper.fireStopRequest();
    }

    /**
     * Stops a running instance of this Collector. The caller can be a
     * different JVM instance than the instance we want to stop.
     */
    public void stop() {
        if (!isRunning()) {
            LOG.info("CANNOT STOP: Collector is not running.");
            return;
        }

        Thread.currentThread().setName(getId() + "/STOP");
        eventManager.fire(new CollectorEvent.Builder(
                CollectorEvent.COLLECTOR_STOP_BEGIN, this).build());

        try {
            getCrawlers().forEach(Crawler::stop);
        } finally {
            try {
                eventManager.fire(new CollectorEvent.Builder(
                        CollectorEvent.COLLECTOR_STOP_END, this).build());
                destroyCollector();
            } finally {
                stopper.destroy();
            }
        }
    }

    /**
     * Gets the event manager.
     * @return event manager
     * @since 2.0.0
     */
    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Loads all crawlers from configuration.
     */
    private void createCrawlers() {
        if (getCrawlers().isEmpty()) {
            List<CrawlerConfig> crawlerConfigs =
                    this.collectorConfig.getCrawlerConfigs();
            if (crawlerConfigs != null) {
                for (CrawlerConfig crawlerConfig : crawlerConfigs) {
                    crawlers.add(createCrawler(crawlerConfig));
                }
            }
        } else {
            LOG.debug("Crawlers already created.");
        }
    }


    /**
     * Creates a new crawler instance.
     * @param config crawler configuration
     * @return new crawler
     */
    protected abstract Crawler createCrawler(CrawlerConfig config);

    //TODO Since 3.0.0
    public CachedStreamFactory getStreamFactory() {
        return streamFactory;
    }

    /**
     * Gets the collector configuration.
     * @return collector configuration
     */
    public CollectorConfig getCollectorConfig() {
        return collectorConfig;
    }

    /**
     * Gets the collector unique identifier.
     * @return collector unique identifier
     */
    public String getId() {
        return collectorConfig.getId();
    }

    /**
     * Gets all crawler instances in this collector.
     * @return crawlers
     */
    public List<Crawler> getCrawlers() {
        return Collections.unmodifiableList(crawlers);
    }

    public String getVersion() {
        return VersionUtil.getVersion(getClass(), "Undefined");
    }

    public String getReleaseVersions() {
        StringBuilder b = new StringBuilder()
            .append(NORCONEX_ASCII)
            .append("\nCollector and main components:\n")
            .append("\n");
        releaseVersions().stream().forEach(s -> b.append(s + '\n'));
        return b.toString();
    }

    private List<String> releaseVersions() {
        List<String> versions = new ArrayList<>();
        versions.add(releaseVersion("Collector", getClass()));
        versions.add(releaseVersion("Collector Core", Collector.class));
        versions.add(releaseVersion("Importer", Importer.class));
        versions.add(releaseVersion("Lang", ClassFinder.class));
        versions.add("Committer(s):");
        versions.add(releaseVersion("  Core", ICommitter.class));
        for (Class<?> c : nonCoreClasspathCommitters()) {
            versions.add(releaseVersion("  " + StringUtils.removeEndIgnoreCase(
                    c.getSimpleName(), "Committer"), c));
        }
        versions.add("Runtime:");
        versions.add("  Name:             " + SystemUtils.JAVA_RUNTIME_NAME);
        versions.add("  Version:          " + SystemUtils.JAVA_RUNTIME_VERSION);
        versions.add("  Vendor:           " + SystemUtils.JAVA_VENDOR);
        return versions;
    }
    private String releaseVersion(String moduleName, Class<?> cls) {
        return StringUtils.rightPad(moduleName + ": ", 20, ' ')
                + VersionUtil.getDetailedVersion(cls, "undefined");
    }

    private Set<Class<?>> nonCoreClasspathCommitters() {
        Set<Class<?>> classes = new HashSet<>();
        if (collectorConfig == null) {
            return classes;
        }
        collectorConfig.getCrawlerConfigs().forEach(crawlerConfig -> {
            crawlerConfig.getCommitters().forEach(committer -> {
                if (!committer.getClass().getName().startsWith(
                        "com.norconex.committer.core")) {
                    classes.add(committer.getClass());
                }
            });
        });
        return classes;
    }

    protected synchronized void lock() {
        LOG.debug("Locking collector execution...");
        lock = new FileLocker(getWorkDir().resolve(".collector-lock"));
        try {
            lock.lock();
        } catch (FileAlreadyLockedException e) {
            throw new CollectorException(
                    "The collector you are attempting to run is already "
                  + "running or executing a command. Wait for "
                  + "it to complete or stop it and try again.");
        } catch (IOException e) {
            throw new CollectorException(
                    "Could not create a collector execution lock.", e);
        }
        LOG.debug("Collector execution locked");
    }
    protected synchronized void unlock() {
        try {
            if (lock != null) {
                lock.unlock();
            }
        } catch (IOException e) {
            throw new CollectorException(
                    "Cannot unlock collector execution.", e);
        }
        lock = null;
        LOG.debug("Collector execution unlocked");
    }

    public boolean isRunning() {
        return lock != null && lock.isLocked();
    }

    @Override
    public String toString() {
        return getId();
    }
}
