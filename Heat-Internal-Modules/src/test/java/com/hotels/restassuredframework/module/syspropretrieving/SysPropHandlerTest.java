/**
 * Copyright (C) 2015-2017 Expedia Inc.
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
package com.hotels.restassuredframework.module.syspropretrieving;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests on SysPropHandler class.
 * @author adebiase
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(SysPropHandler.class)
public class SysPropHandlerTest {
    private static final String PROPERTY_TO_TEST = "propertyToTest";

    @InjectMocks
    private SysPropHandler underTest;

    @BeforeMethod
    public void setUp() {
        System.clearProperty(PROPERTY_TO_TEST);
    }

    @Test(enabled = true)
    public void testStringWithoutPlaceholder() {
        underTest = new SysPropHandler("[testPlaceholderWithoutSysPropName]");
        String outputStr = underTest.processString("StringWithoutPlaceholder");
        Assert.assertEquals(outputStr, "StringWithoutPlaceholder");
    }


    @Test(enabled = true)
    public void testPlaceholderWithoutSysPropName() {
        underTest = new SysPropHandler("[testPlaceholderWithoutSysPropName]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithEmptySysPropName() {
        underTest = new SysPropHandler("[testPlaceholderWithEmptySysPropName]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[]}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithoutSysPropName1() {
        underTest = new SysPropHandler("[testPlaceholderWithoutSysPropName1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithEmptySysPropName1() {
        underTest = new SysPropHandler("[testPlaceholderWithEmptySysPropName1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[]}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropNotSetAndNoDefault() {
        underTest = new SysPropHandler("[testPlaceholderWithSysPropNotSetAndNoDefault]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + "]}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropNotSetAndNoDefault1() {
        underTest = new SysPropHandler("[testPlaceholderWithSysPropNotSetAndNoDefault1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + "]}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, inputString);
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSet() {
        //GIVEN
        System.setProperty(PROPERTY_TO_TEST, "PIPPO");

        underTest = new SysPropHandler("[testPlaceholderWithSysPropSet]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + "]}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "PIPPO");
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSet1() {
        //GIVEN
        System.setProperty(PROPERTY_TO_TEST, "PIPPO");

        underTest = new SysPropHandler("[testPlaceholderWithSysPropSet1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + "]}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "STRING_PIPPO_STRING");
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSetToDefault() {
        underTest = new SysPropHandler("[testPlaceholderWithSysPropSetToDefault]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + ",pippo_default]}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "pippo_default");
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSetToDefault1() {
        underTest = new SysPropHandler("[testPlaceholderWithSysPropSetToDefault1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + ",pippo_default]}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "STRING_pippo_default_STRING");
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSetAndDefaultSet() {
        //GIVEN
        System.setProperty(PROPERTY_TO_TEST, "PIPPO");

        underTest = new SysPropHandler("[testPlaceholderWithSysPropSetAndDefaultSet]");
        String inputString = SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + ", pippo_default]}";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "PIPPO");
    }

    @Test(enabled = true)
    public void testPlaceholderWithSysPropSetAndDefaultSet1() {
        //GIVEN
        System.setProperty(PROPERTY_TO_TEST, "PIPPO");

        underTest = new SysPropHandler("[testPlaceholderWithSysPropSetAndDefaultSet1]");
        String inputString = "STRING_" + SysPropHeatPlaceholderModule.SYS_PROP_PLACEHOLDER + "[" + PROPERTY_TO_TEST + ", pippo_default]}_STRING";
        String outputStr = underTest.processString(inputString);
        Assert.assertEquals(outputStr, "STRING_PIPPO_STRING");
    }

}
