/* Copyright 2016-2018 Norconex Inc.
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

public class MockSuiteLifeCycleListener { // implements ISuiteLifeCycleListener {

    private final String test = "MockSuiteLifeCycleListener";

//    @Override
//    public void suiteStopped(JobSuite suite) {
//    }
//
//    @Override
//    public void suiteStopping(JobSuite suite) {
//    }
//
//    @Override
//    public void suiteStarted(JobSuite suite) {
//    }
//
//    @Override
//    public void suiteAborted(JobSuite suite) {
//    }
//
//    @Override
//    public void suiteTerminatedPrematuraly(JobSuite suite) {
//    }
//
//    @Override
//    public void suiteCompleted(JobSuite suite) {
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((test == null) ? 0 : test.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MockSuiteLifeCycleListener other = (MockSuiteLifeCycleListener) obj;
        if (test == null) {
            if (other.test != null) {
                return false;
            }
        } else if (!test.equals(other.test)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MockSuiteLifeCycleListener [test=" + test + "]";
    }
}
