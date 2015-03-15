package demo.binea.com.instagramwithmaterialdesign.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewTreeObserver;

import butterknife.ButterKnife;
import butterknife.InjectView;
import demo.binea.com.instagramwithmaterialdesign.R;
import demo.binea.com.instagramwithmaterialdesign.adapter.UserProfileAdapter;
import demo.binea.com.instagramwithmaterialdesign.view.RevealBackgroundView;

/**
 * Created by xubinggui on 15/3/15.
 */
public class UserProfileActivity extends FragmentActivity implements RevealBackgroundView.OnStateChangeListener {
	public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

	@InjectView(R.id.vRevealBackground)
	RevealBackgroundView vRevealBackground;
	@InjectView(R.id.rvUserProfile)
	RecyclerView rvUserProfile;

	private UserProfileAdapter userPhotosAdapter;

	public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
		Intent intent = new Intent(startingActivity, UserProfileActivity.class);
		intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
		startingActivity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		ButterKnife.inject(this);
		setupUserProfileGrid();
		setupRevealBackground(savedInstanceState);
	}

	private void setupRevealBackground(Bundle savedInstanceState) {
		vRevealBackground.setOnStateChangeListener(this);
		if (savedInstanceState == null) {
			final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
			vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
					vRevealBackground.startFromLocation(startingLocation);
					return false;
				}
			});
		} else {
			userPhotosAdapter.setLockedAnimations(true);
			vRevealBackground.setToFinishedFrame();
		}
	}

	private void setupUserProfileGrid() {
		final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
		rvUserProfile.setLayoutManager(layoutManager);
		rvUserProfile.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				userPhotosAdapter.setLockedAnimations(true);
			}
		});
	}

	@Override
	public void onStateChange(int state) {
		if (RevealBackgroundView.STATE_FINISHED == state) {
			rvUserProfile.setVisibility(View.VISIBLE);
			userPhotosAdapter = new UserProfileAdapter(this);
			rvUserProfile.setAdapter(userPhotosAdapter);
		} else {
			rvUserProfile.setVisibility(View.INVISIBLE);
		}
	}
}