/*  
 *    Copyright (C) 2013 - 2014 Shaleen Jain <shaleen.jain95@gmail.com>
 *
 *	  This file is part of UPES Academics.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/    

package com.shalzz.attendance.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.R;
import com.shalzz.attendance.fragment.AttendanceListFragment;
import com.shalzz.attendance.fragment.SettingsFragment;
import com.shalzz.attendance.fragment.TimeTablePagerFragment;
import com.shalzz.attendance.model.ListHeader;
import com.shalzz.attendance.wrapper.MyVolley;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class MainActivity extends SherlockFragmentActivity {

	private String mTag = "Main Activity";
	private String[] mNavTitles;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private View Drawerheader;
	private FragmentManager mFragmentManager;
	public static MainActivity mActivity;
	private static final String BUNDLE_KEY_PREVIOUS_FRAGMENT = "MainActivity.PREVIOUS_FRAGMENT";
	private static final String FRAGMENT_TAG = "MainActivity.FRAGMENT_TAG";
	private static final String PREFERENCE_ACTIVATED_FRAGMENT = "ACTIVATED_FRAGMENT";
	private Boolean DEBUG_FRAGMENTS = true;

	// Our custom poor-man's back stack which has only one entry at maximum.
	private Fragment mPreviousFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drawer);

		mNavTitles = getResources().getStringArray(R.array.drawer_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		mFragmentManager = getSupportFragmentManager();

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Drawerheader = inflater.inflate(R.layout.drawer_header, null);
		if(mDrawerList.getHeaderViewsCount()==0)
			mDrawerList.addHeaderView(Drawerheader);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item,R.id.drawer_list_textView, mNavTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mTitle  = getTitle();
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				actionbar.setTitle(mTitle);
				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				actionbar.setTitle(mDrawerTitle);
				supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		reloadCurrentFragment();

		updateDrawerHeader();
		mActivity =this;

		//getAttendance(); // TODO: needed?
		//		
		//		ViewTarget target = new ViewTarget(R.id.tvSubj,this);
		//		PointTarget pt = new PointTarget(20, 50);
		//		ShowcaseView.insertShowcaseView(pt, this, "Details", "Touch a Subject for more details about it");
	}

	public static MainActivity getInstance(){
		return mActivity;
	}

	public void updateDrawerHeader() {
		DatabaseHandler db = new DatabaseHandler(this);
		if(db.getRowCount()>0) {
			ListHeader listheader = db.getListHeader();

			TextView tv_name = (TextView) Drawerheader.findViewById(R.id.drawer_header_name);
			TextView tv_course = (TextView) Drawerheader.findViewById(R.id.drawer_header_course);
			tv_name.setText(listheader.getName());
			tv_course.setText(listheader.getCourse());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home)
		{
			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		}
		return super.onOptionsItemSelected(item);
	}


	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			displayView(position);
		}
	}

	public void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			return;
		case 1:
			fragment = new AttendanceListFragment();
			break;
		case 2:
			fragment = new TimeTablePagerFragment();
			break;
		case 3:
			fragment = new SettingsFragment();
			break;
		case 4:
			// Helpshift.showFAQs(this);
			return;

		default:
			break;
		}

		if (fragment != null) {
			// show the fragment
			showFragment(fragment);

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			mDrawerTitle = mNavTitles[position-1];
			//mDrawerList.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.list_background_pressed));
			setTitle(mDrawerTitle);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			Log.e(mTag, "Error in creating fragment");
		}
	}
	
	private void persistCurrentFragment() {
		SharedPreferences settings = this.getSharedPreferences("SETTINGS", 0);
		SharedPreferences.Editor editor = settings.edit();
		int fragmentPosition = 1;
		Fragment installed = getInstalledFragment();
		
		if(installed instanceof AttendanceListFragment) {
			fragmentPosition = 1;
		} else if(installed instanceof TimeTablePagerFragment) {
			fragmentPosition = 2;
		}		
		
		if (DEBUG_FRAGMENTS) {
			Log.i(mTag, this + " persistCurrentFragment: Saving fragment " + installed + " at position " + fragmentPosition);
		}
		
		editor.putInt(PREFERENCE_ACTIVATED_FRAGMENT, fragmentPosition);
		editor.commit();
	}

	private void reloadCurrentFragment() {
		SharedPreferences settings = this.getSharedPreferences("SETTINGS", 0);
		int fragmentPosition = settings.getInt(PREFERENCE_ACTIVATED_FRAGMENT, 1);
		
		if (DEBUG_FRAGMENTS) {
			Log.i(mTag, this + " reloadCurrentFragment: Restoring fragment positon " + fragmentPosition);
		}
		
		displayView(fragmentPosition);
		}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPreviousFragment != null) {
			mFragmentManager.putFragment(outState,
					BUNDLE_KEY_PREVIOUS_FRAGMENT, mPreviousFragment);
			if (DEBUG_FRAGMENTS) {
				Log.i(mTag, this + " showFragment: Saving fragment " + mPreviousFragment);
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	/**
	 * Push the installed fragment into our custom back stack (or optionally
	 * {@link FragmentTransaction#remove} it) and {@link FragmentTransaction#add} {@code fragment}.
	 *
	 * @param fragment {@link Fragment} to be added.
	 *
	 */
	private void showFragment(Fragment fragment) {
		final FragmentTransaction ft = mFragmentManager.beginTransaction();
		final Fragment installed = getInstalledFragment();

		// return if the fragment is already installed 
		if(isAttendanceListInstalled() && fragment instanceof AttendanceListFragment ||
		   isTimeTablePagerInstalled() && fragment instanceof TimeTablePagerFragment ||
		   isSettingsInstalled() && fragment instanceof SettingsFragment) {
			
			if (DEBUG_FRAGMENTS) {
				Log.i(mTag, this + " showFragment: " + fragment + " is already installed");
			}
			return;
		}

		if (DEBUG_FRAGMENTS) {
			Log.i(mTag, this + " backstack: [push] " + installed
					+ " -> " + fragment);
		}

		if (mPreviousFragment != null) {
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " showFragment: destroying previous fragment "
						+ mPreviousFragment);
			}
			ft.remove(mPreviousFragment);
			mPreviousFragment = null;
		}

		// Remove the current fragment or push it into the backstack.
		if (installed != null) {
			mPreviousFragment = installed;
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " showFragment: detaching "
						+ mPreviousFragment);
			}
			ft.detach(mPreviousFragment);
		}
		// Show the new one
		if (DEBUG_FRAGMENTS) {
			Log.d(mTag, this + " showFragment: replacing with " + fragment);
		}
		ft.replace(R.id.frame_container, fragment, FRAGMENT_TAG);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	@Override
	public void onBackPressed() {
		// Custom back stack
		if (shouldPopFromBackStack(true)) {
			if (DEBUG_FRAGMENTS) {
				Log.d(mTag, this + " Back: Popping from back stack");
			}
			popFromBackStack();
		}
		else
			super.onBackPressed();
	}

	/**
	 * @param isSystemBackKey <code>true</code> if the system back key was pressed.
	 *        <code>false</code> if it's caused by the "home" icon click on the action bar.
	 * @return true if we should pop from our custom back stack.
	 */
	private boolean shouldPopFromBackStack(boolean isSystemBackKey) {

		if (mPreviousFragment == null) {
			return false; // Nothing in the back stack
		}
		final Fragment installed = getInstalledFragment();
		if (installed == null) {
			// If no fragment is installed right now, do nothing.
			return false;
		}
		// Okay now we have 2 fragments; the one in the back stack and the one that's currently
		// installed.
		if (installed instanceof AttendanceListFragment ||
				installed instanceof TimeTablePagerFragment) {
			// These are the top level lists - never go back from here.
			return false;
		}

		return true;
	}

	/**
	 * Pop from our custom back stack.
	 */
	private void popFromBackStack() {
		if (mPreviousFragment == null) {
			return;
		}
		final FragmentTransaction ft = mFragmentManager.beginTransaction();
		final Fragment installed = getInstalledFragment();     
		int position = 0 ;
		Log.i(mTag, this + " backstack: [pop] " + installed + " -> "
				+ mPreviousFragment);
		ft.remove(installed);
		// Restore listContext.
		if (mPreviousFragment instanceof AttendanceListFragment) {
			position = 1;
		} else if (mPreviousFragment instanceof TimeTablePagerFragment) {
			position = 2;
		} else if (mPreviousFragment instanceof SettingsFragment) {
			position = 3;
		}
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position-1);
		mDrawerTitle = mNavTitles[position-1];
		setTitle(mDrawerTitle);

		ft.attach(mPreviousFragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		mPreviousFragment = null;
		ft.commit();
		return;
	}

	/**
	 * @return currently installed {@link Fragment} (1-pane has only one at most), or null if none
	 *         exists.
	 */
	private Fragment getInstalledFragment() {
		return mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
	}

	private boolean isAttendanceListInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
		if (mFragment instanceof AttendanceListFragment) {
			return true;
		}
		return false;   	
	}
	
	private boolean isTimeTablePagerInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
		if (mFragment instanceof TimeTablePagerFragment) {
			return true;
		}
		return false;   	
	}
	
	private boolean isSettingsInstalled() {
		Fragment mFragment = mFragmentManager.findFragmentByTag(FRAGMENT_TAG);
		if (mFragment instanceof SettingsFragment) {
			return true;
		}
		return false;   	
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onDestroy() {
		MyVolley.getInstance().cancelPendingRequests("com.shalzz.attendance.AttendanceListFragment");
		Crouton.cancelAllCroutons();
		persistCurrentFragment();
		super.onDestroy();
	}

}