package demo.binea.com.instagramwithmaterialdesign.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import demo.binea.com.instagramwithmaterialdesign.R;
import demo.binea.com.instagramwithmaterialdesign.Util;
import demo.binea.com.instagramwithmaterialdesign.adapter.CommentsAdapter;
import demo.binea.com.instagramwithmaterialdesign.view.SendCommentButton;

/**
 * Created by xubinggui on 15/3/14.
 */
public class CommentsActivity extends ActionBarActivity implements SendCommentButton.OnSendClickListener {

	public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";

	@InjectView(R.id.contentRoot)
	LinearLayout contentRoot;
	@InjectView(R.id.rvComments)
	RecyclerView rvComments;
	@InjectView(R.id.llAddComment)
	LinearLayout llAddComment;
	@InjectView(R.id.etComment)
	EditText etComment;
	@InjectView(R.id.btnSendComment)
	SendCommentButton btnSendComment;

	private CommentsAdapter commentsAdapter;
	private int drawingStartLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		ButterKnife.inject(this);
		setupComments();
		setupSendCommentButton();

		drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
		if (savedInstanceState == null) {
			contentRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					contentRoot.getViewTreeObserver().removeOnPreDrawListener(this);
					startIntroAnimation();
					return true;
				}
			});
		}
	}

	private void setupComments() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
		rvComments.setLayoutManager(linearLayoutManager);
		rvComments.setHasFixedSize(true);

		commentsAdapter = new CommentsAdapter(this);
		rvComments.setAdapter(commentsAdapter);
		rvComments.setOverScrollMode(View.OVER_SCROLL_NEVER);
		rvComments.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
					commentsAdapter.setAnimationsLocked(true);
				}
			}
		});
	}

	private void setupSendCommentButton() {
		btnSendComment.setOnSendClickListener(this);
	}

	private void startIntroAnimation() {
//		ViewCompat.setElevation(getToolbar(), 0);
		contentRoot.setScaleY(0.1f);
		contentRoot.setPivotY(drawingStartLocation);
		llAddComment.setTranslationY(200);

		contentRoot.animate()
				.scaleY(1)
				.setDuration(200)
				.setInterpolator(new AccelerateInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
//						ViewCompat.setElevation(getToolbar(), Util.dpToPx(8));
						animateContent();
					}
				})
				.start();
	}

	private void animateContent() {
		commentsAdapter.updateItems();
		llAddComment.animate().translationY(0)
				.setInterpolator(new DecelerateInterpolator())
				.setDuration(200)
				.start();
	}

	@Override
	public void onBackPressed() {
//		ViewCompat.setElevation(getToolbar(), 0);
		contentRoot.animate()
				.translationY(Util.getScreenHeight(this))
				.setDuration(200)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						CommentsActivity.super.onBackPressed();
						overridePendingTransition(0, 0);
					}
				})
				.start();
	}

	@Override
	public void onSendClickListener(View v) {
		if (validateComment()) {
			commentsAdapter.addItem();
			commentsAdapter.setAnimationsLocked(false);
			commentsAdapter.setDelayEnterAnimation(false);
			rvComments.smoothScrollBy(0, rvComments.getChildAt(0).getHeight() * commentsAdapter.getItemCount());

			etComment.setText(null);
			btnSendComment.setCurrentState(SendCommentButton.STATE_DONE);
		}
	}

	private boolean validateComment() {
		if (TextUtils.isEmpty(etComment.getText())) {
			btnSendComment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
			return false;
		}

		return true;
	}
}
