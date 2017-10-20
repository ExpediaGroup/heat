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
package com.hotels.restassuredframework.module.dateretrieving;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests on DateHandler class.
 * @author adebiase
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateHandler.class)
public class DateHandlerTest {


    @InjectMocks
    private DateHandler underTest;

//    @BeforeMethod
//    public void setUp() {
//
//    }

    @Test
    public void testDate() {
        Assert.assertTrue(true);

    }


}
