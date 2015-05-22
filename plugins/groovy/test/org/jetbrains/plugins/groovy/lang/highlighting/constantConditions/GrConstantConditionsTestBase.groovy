/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.groovy.lang.highlighting.constantConditions

import com.intellij.codeInspection.InspectionProfileEntry
import groovy.transform.CompileStatic
import org.jetbrains.plugins.groovy.codeInspection.dataflow.GrConstantConditionsInspection
import org.jetbrains.plugins.groovy.lang.highlighting.GrHighlightingTestBase

@CompileStatic
abstract class GrConstantConditionsTestBase extends GrHighlightingTestBase {

  InspectionProfileEntry[] customInspections

  String basePath = super.basePath + 'constantConditions/'

  void doTest() {
    myFixture.enableInspections(customInspections)
    def name = getName().split()[1..-1].collect { it[0].toUpperCase() + it[1..-1] }.join('')
    myFixture.testHighlighting(true, false, true, getTestName(name, true) + ".groovy");
  }

  GrConstantConditionsTestBase() {
    def inspection = new GrConstantConditionsInspection()
    inspection.UNKNOWN_MEMBERS_ARE_NULLABLE = false
    customInspections = [inspection] as InspectionProfileEntry[]
  }
}

