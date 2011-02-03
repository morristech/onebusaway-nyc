/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.nyc.integration_tests.vehicle_tracking_webapp.cases;

/**
 * Trace: 7564-2010-12-02T11-49-09
 * 
 * block = 2008_12888581
 * 
 * Interesting trace because it goes off route (deviation onto 4th Ave from 60th
 * to 50th)
 * 
 * @author bdferris
 */
public class Trace_7564_20101202T114909_IntegrationTest extends AbstractTraceRunner {

  public Trace_7564_20101202T114909_IntegrationTest() {
    super("7564-2010-12-02T11-49-09.csv.gz");
  }
}