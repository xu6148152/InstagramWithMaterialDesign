package demo.binea.com.instagramwithmaterialdesign.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import demo.binea.com.instagramwithmaterialdesign.Util;

/**
 * Created by xubinggui on 15/3/15.
 */
public class FeedContextMenuManager extends RecyclerView.OnScrollListener implements View.OnAttachStateChangeListener {

	private FeedContextMenu contextMenuView;
	private boolean isContextMenuShowing = false;
	private boolean isContextMenuDismissing = false;

	public static FeedContextMenuManager getInstance(){
		return FeedContextMenuManagerHolder.mFeedContextMenuManager;
	}

	private static class FeedContextMenuManagerHolder{
		private static FeedContextMenuManager mFeedContextMenuManager = new FeedContextMenuManager();
	}


	@Override
	public void onViewAttachedToWindow(View v) {

	}

	@Override
	public void onViewDetachedFromWindow(View v) {
		contextMenuView = null;
	}

	public void toggleContextMenuFromView(View openingView, int feedItem, FeedContextMenu.OnFeedContextMenuItemClickListener listener) {
		if (contextMenuView == null) {
			showContextMenuFromView(openingView, feedItem, listener);
		} else {
			hideContextMenu();
		}
	}

	private void showContextMenuFromView(final View openingView, int feedItem, FeedContextMenu.OnFeedContextMenuItemClickListener listener) {
		if (!isContextMenuShowing) {
			isContextMenuShowing = true;
			contextMenuView = new FeedContextMenu(openingView.getContext());
			contextMenuView.bindToItem(feedItem);
			contextMenuView.addOnAttachStateChangeListener(this);
			contextMenuView.setOnFeedMenuItemClickListener(listener);

			((ViewGroup) openingView.getRootView().findViewById(android.R.id.content)).addView(contextMenuView);

			contextMenuView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					contextMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
					setupContextMenuInitialPosition(openingView);
					performShowAnimation();
					return false;
				}
			});
		}
	}

	private void setupContextMenuInitialPosition(View openingView) {
		final int[] openingViewLocation = new int[2];
		openingView.getLocationOnScreen(openingViewLocation);
		int additionalBottomMargin = Util.dpToPx(16);
		contextMenuView.setTranslationX(openingViewLocation[0] - contextMenuView.getWidth() / 3);
		contextMenuView.setTranslationY(openingViewLocation[1] - contextMenuView.getHeight() - additionalBottomMargin);
	}

	private void performShowAnimation() {
		contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
		contextMenuView.setPivotY(contextMenuView.getHeight());
		contextMenuView.setScaleX(0.1f);
		contextMenuView.setScaleY(0.1f);
		contextMenuView.animate()
				.scaleX(1f).scaleY(1f)
				.setDuration(150)
				.setInterpolator(new OvershootInterpolator())
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						isContextMenuShowing = false;
					}
				});
	}

	public void hideContextMenu() {
		if (!isContextMenuDismissing && contextMenuView != null) {
			isContextMenuDismissing = true;
			performDismissAnimation();
		}
	}

	private void performDismissAnimation() {
		contextMenuView.setPivotX(contextMenuView.getWidth() / 2);
		contextMenuView.setPivotY(contextMenuView.getHeight());
		contextMenuView.animate()
				.scaleX(0.1f).scaleY(0.1f)
				.setDuration(150)
				.setInterpolator(new AccelerateInterpolator())
				.setStartDelay(100)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if (contextMenuView != null) {
							contextMenuView.dismiss();
							contextMenuView = null;
						}
						isContextMenuDismissing = false;
					}
				});
	}

	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		if (contextMenuView != null) {
			hideContextMenu();
			contextMenuView.setTranslationY(contextMenuView.getTranslationY() - dy);
		}
	}
}
