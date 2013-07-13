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

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.woltage.irssiconnectbot.bean.SelectionArea;

class ICBOnTouchListener implements View.OnTouchListener {

	private final RelativeLayout keyboardGroup;
	private final GestureDetector detect;
	private final ActionBar actionBar;
	private final Handler handler = new Handler();
	private final Runnable hider;

	private float lastX, lastY;
	private int lastTouchRow, lastTouchCol;
	private ConsoleActivity consoleActivity;

	private class Hider implements Runnable {
		public void run() {
			if (keyboardGroup.getVisibility() == View.GONE)
				return;

			keyboardGroup.startAnimation(consoleActivity.keyboard_fade_out);
			keyboardGroup.setVisibility(View.GONE);
			if (actionBar != null) {
				actionBar.hide();
			}
		}
	}

	public ICBOnTouchListener (ConsoleActivity consoleActivity, RelativeLayout keyboardGroup,
							   ActionBar actionBar, GestureDetector detect) {
		this.consoleActivity = consoleActivity;
		this.keyboardGroup = keyboardGroup;
		this.actionBar = actionBar;
		this.detect = detect;
		this.hider = new Hider();

		if (actionBar != null) {
			actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
					public void onMenuVisibilityChanged(boolean isVisible) {
						if (isVisible) {
							handler.removeCallbacks(hider);
						} else {
							handler.postDelayed(hider, ConsoleActivity.KEYBOARD_DISPLAY_TIME);
						}
					}
				});
		}
	}

	public boolean onTouch(View v, MotionEvent event) {

		// when copying, highlight the area
		if (consoleActivity.copySource != null && consoleActivity.copySource.isSelectingForCopy()) {
			int row = (int) Math.floor(event.getY() / consoleActivity.copySource.charHeight);
			int col = (int) Math.floor(event.getX() / consoleActivity.copySource.charWidth);

			SelectionArea area = consoleActivity.copySource.getSelectionArea();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// recording starting area
				if (area.isSelectingOrigin()) {
					area.setRow(row);
					area.setColumn(col);
					lastTouchRow = row;
					lastTouchCol = col;
					consoleActivity.copySource.redraw();
				}
				return true;
			case MotionEvent.ACTION_MOVE:
				/*
				 * ignore when user hasn't moved since last time so we
				 * can fine-tune with directional pad
				 */
				if (row == lastTouchRow && col == lastTouchCol)
					return true;

				// if the user moves, start the selection for other
				// corner
				area.finishSelectingOrigin();

				// update selected area
				area.setRow(row);
				area.setColumn(col);
				lastTouchRow = row;
				lastTouchCol = col;
				consoleActivity.copySource.redraw();
				return true;
			case MotionEvent.ACTION_UP:
				/*
				 * If they didn't move their finger, maybe they meant to
				 * select the rest of the text with the directional pad.
				 */
				if (area.getLeft() == area.getRight() && area.getTop() == area.getBottom()) {
					return true;
				}

				// copy selected area to clipboard
				String copiedText = area.copyFrom(consoleActivity.copySource.buffer);

				consoleActivity.clipboard.setText(copiedText);
				Toast.makeText(consoleActivity, consoleActivity.getString(R.string.console_copy_done, copiedText.length()), Toast.LENGTH_LONG).show();
				// fall through to clear state

			case MotionEvent.ACTION_CANCEL:
				// make sure we clear any highlighted area
				area.reset();
				consoleActivity.copySource.setSelectingForCopy(false);
				consoleActivity.copySource.redraw();
				return true;
			}
		}

		//Configuration config = consoleActivity.getResources().getConfiguration();

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			lastX = event.getX();
			lastY = event.getY();
		} else if (event.getAction() == MotionEvent.ACTION_UP
				&& keyboardGroup.getVisibility() == View.GONE
				&& event.getEventTime() - event.getDownTime() < ConsoleActivity.CLICK_TIME
				&& Math.abs(event.getX() - lastX) < ConsoleActivity.MAX_CLICK_DISTANCE
				&& Math.abs(event.getY() - lastY) < ConsoleActivity.MAX_CLICK_DISTANCE) {
			keyboardGroup.startAnimation(consoleActivity.keyboard_fade_in);
			keyboardGroup.setVisibility(View.VISIBLE);
			if (actionBar != null) {
				actionBar.show();
			}

			handler.postDelayed(this.hider, ConsoleActivity.KEYBOARD_DISPLAY_TIME);
		}

		// pass any touch events back to detector
		return detect.onTouchEvent(event);
	}

}
