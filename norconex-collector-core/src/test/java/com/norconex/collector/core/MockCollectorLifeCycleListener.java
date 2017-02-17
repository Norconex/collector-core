/* Copyright 2017 Norconex Inc.
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

public class MockCollectorLifeCycleListener 
        implements ICollectorLifeCycleListener {

    private String test = "MockCollectorLifeCycleListener";

    @Override
    public void onCollectorStart(ICollector collector) {
    }

    @Override
    public void onCollectorFinish(ICollector collector) {
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
        MockCollectorLifeCycleListener other = 
                (MockCollectorLifeCycleListener) obj;
        if (test == null) {
            if (other.test != null)
                return false;
        } else if (!test.equals(other.test))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MockCollectorLifeCycleListener [test=" + test + "]";
    }
}
