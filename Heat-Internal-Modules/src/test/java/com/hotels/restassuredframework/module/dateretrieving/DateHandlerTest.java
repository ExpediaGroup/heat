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
