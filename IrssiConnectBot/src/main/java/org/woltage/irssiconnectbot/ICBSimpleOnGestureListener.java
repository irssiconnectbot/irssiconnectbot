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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.view.ViewConfiguration;
import de.mud.terminal.vt320;
import org.woltage.irssiconnectbot.service.TerminalKeyListener;

class ICBSimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
	private float totalY = 0;
	private ConsoleActivity consoleActivity;

	public ICBSimpleOnGestureListener (ConsoleActivity consoleActivity) {this.consoleActivity = consoleActivity;}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2,
			float velocityX, float velocityY) {

		final float distx = e2.getRawX() - e1.getRawX();
		final float disty = e2.getRawY() - e1.getRawY();
		final int goalwidth = consoleActivity.flip.getWidth() / 2;

		// need to slide across half of display to trigger
		// console change
		// make sure user kept a steady hand horizontally
		if (Math.abs(disty) < (consoleActivity.flip.getHeight() / 4)) {
			if (distx > goalwidth) {
				consoleActivity.shiftCurrentTerminal(ConsoleActivity.SHIFT_RIGHT);
				return true;
			}

			if (distx < -goalwidth) {
				consoleActivity.shiftCurrentTerminal(ConsoleActivity.SHIFT_LEFT);
				return true;
			}

		}

		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		// if copying, then ignore
		if (consoleActivity.copySource != null && consoleActivity.copySource.isSelectingForCopy())
			return false;

		if (e1 == null || e2 == null)
			return false;

		// if releasing then reset total scroll
		if (e2.getAction() == MotionEvent.ACTION_UP) {
			totalY = 0;
		}

		// activate consider if within x tolerance
		if (Math.abs(e1.getX() - e2.getX()) < ViewConfiguration.getTouchSlop() * 4) {

			View flip = consoleActivity.findCurrentView(R.id.console_flip);
			if (flip == null) return false;
			TerminalView terminal = (TerminalView) flip;

			// estimate how many rows we have scrolled through
			// accumulate distance that doesn't trigger
			// immediate scroll
			totalY += distanceY;
			final int moved = (int) (totalY / terminal.bridge.charHeight);

			// consume as scrollback only if towards right half
			// of screen
			if (e2.getX() > flip.getWidth() / 2) {
				if (moved != 0) {
					int base = terminal.bridge.buffer.getWindowBase();
					terminal.bridge.buffer.setWindowBase(base + moved);
					totalY = 0;
					return true;
				}
			} else {
				// otherwise consume as pgup/pgdown for every 5
				// lines
				if (moved > 5) {
					((vt320) terminal.bridge.buffer).keyPressed(vt320.KEY_PAGE_DOWN, ' ', 0);
					terminal.bridge.tryKeyVibrate();
					totalY = 0;
					return true;
				} else if (moved < -5) {
					((vt320) terminal.bridge.buffer).keyPressed(vt320.KEY_PAGE_UP, ' ', 0);
					terminal.bridge.tryKeyVibrate();
					totalY = 0;
					return true;
				}

			}

		}

		return false;
	}

	/*
			 * Enables doubletap = ESC+a
			 *
			 * @see
			 * android.view.GestureDetector.SimpleOnGestureListener#
			 * onDoubleTap(android.view.MotionEvent)
			 *
			 * @return boolean
			 */
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		View flip = consoleActivity.findCurrentView(R.id.console_flip);
		if (flip == null) return false;
		TerminalView terminal = (TerminalView) flip;

		((vt320) terminal.bridge.buffer).keyTyped(vt320.KEY_ESCAPE, ' ', 0);
		((vt320) terminal.bridge.buffer).write('a');

		return true;
	}

	/*
			 * Enables longpress and popups menu
			 *
			 * @see
			 * android.view.GestureDetector.SimpleOnGestureListener#
			 * onLongPress(android.view.MotionEvent)
			 *
			 * @return void
			 */
	@Override
	public void onLongPress(MotionEvent e) {
		if(consoleActivity.prefs.getBoolean("longPressMenu", true)) {
			View flip = consoleActivity.findCurrentView(R.id.console_flip);
			if (flip == null) return;
			TerminalView terminal = (TerminalView) flip;

			terminal.bridge.tryKeyVibrate();

			final CharSequence[] items = { "Alt+?", "TAB",  "Ctrl+?"};

			AlertDialog.Builder builder = new AlertDialog.Builder(consoleActivity);
			builder.setTitle("Send an action");
			builder.setItems(items,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							View flip = consoleActivity.findCurrentView(R.id.console_flip);
							if (flip == null) return;
							TerminalView terminal = (TerminalView) flip;

							if (item == 0) {
								((vt320) terminal.bridge.buffer).keyTyped(vt320.KEY_ESCAPE, ' ', 0);
								terminal.bridge.tryKeyVibrate();
							} else if (item == 1) {
								((vt320) terminal.bridge.buffer).write(0x09);
								terminal.bridge.tryKeyVibrate();
							} else if (item == 2) {
                                TerminalKeyListener handler = terminal.bridge.getKeyHandler();
                                handler.metaPress(TerminalKeyListener.META_CTRL_ON);
                                terminal.bridge.tryKeyVibrate();
							}
						}
					});
			AlertDialog alert = builder.create();

			builder.show();
		}
	}
}
