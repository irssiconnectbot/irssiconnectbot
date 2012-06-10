/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
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

package org.woltage.irssiconnectbot;

import org.woltage.irssiconnectbot.bean.HostBean;
import org.woltage.irssiconnectbot.util.HostDatabase;
import org.woltage.irssiconnectbot.util.PubkeyDatabase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SetupPubkeyActivity extends Activity {
    private final static String TAG = "ConnectBot.SetupPubkeyActivity";

    private HostDatabase hostdb = null;
    private PubkeyDatabase pubkeydb = null;

    private HostBean host;

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_setuppubkey);

        long hostId = this.getIntent().getLongExtra(Intent.EXTRA_TITLE, -1);

        this.hostdb = new HostDatabase(this);
        this.pubkeydb = new PubkeyDatabase(this);

        host = hostdb.findHostById(hostId);

        Log.d(TAG, "host=" + host.getHostname());

    }
}
