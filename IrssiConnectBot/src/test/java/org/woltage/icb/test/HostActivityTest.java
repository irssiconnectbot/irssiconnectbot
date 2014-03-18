package java.org.woltage.icb.test;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
/**
 * Created by woltage on 16.3.2014.
 */
@RunWith(RobolectricTestRunner.class)
public class HostActivityTest {

    @Test
    public void testHosts() throws Exception {
        MainActivity activity;
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
        assertNotNull(activity);
    }
}
