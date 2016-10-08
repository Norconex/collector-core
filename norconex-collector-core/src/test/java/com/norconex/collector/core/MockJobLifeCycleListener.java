/* Copyright 2016 Norconex Inc.
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

import com.norconex.jef4.job.IJobLifeCycleListener;
import com.norconex.jef4.status.IJobStatus;

public class MockJobLifeCycleListener implements IJobLifeCycleListener {

    private String test = "MockJobLifeCycleListener";
    
    @Override
    public void jobStopping(IJobStatus status) {
    }

    @Override
    public void jobStopped(IJobStatus status) {
    }

    @Override
    public void jobStarted(IJobStatus status) {
    }

    @Override
    public void jobResumed(IJobStatus status) {
    }

    @Override
    public void jobSkipped(IJobStatus status) {
    }

    @Override
    public void jobProgressed(IJobStatus status) {
    }

    @Override
    public void jobTerminatedPrematuraly(IJobStatus status) {
    }

    @Override
    public void jobCompleted(IJobStatus status) {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((test == null) ? 0 : test.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MockJobLifeCycleListener other = (MockJobLifeCycleListener) obj;
        if (test == null) {
            if (other.test != null)
                return false;
        } else if (!test.equals(other.test))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MockJobLifeCycleListener [test=" + test + "]";
    }
}
