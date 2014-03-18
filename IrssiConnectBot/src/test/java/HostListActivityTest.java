import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.woltage.irssiconnectbot.HostListActivity;
import org.woltage.irssiconnectbot.R;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HostListActivityTest {
    private HostListActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(HostListActivity.class).create().visible().get();
    }

    @Test
    public void testHostListHasQuickConnectBar() throws Exception {
        EditText editText = (EditText) activity.findViewById(R.id.front_quickconnect);
        editText.setText("woltage");

        assertThat(editText.getText().toString(), equalTo("woltage"));
    }
}
