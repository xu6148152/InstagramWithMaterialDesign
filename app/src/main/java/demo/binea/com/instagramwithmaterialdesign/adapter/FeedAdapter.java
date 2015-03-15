package demo.binea.com.instagramwithmaterialdesign.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import demo.binea.com.instagramwithmaterialdesign.R;
import demo.binea.com.instagramwithmaterialdesign.Util;

/**
 * Created by xubinggui on 15/3/14.
 */
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

	private static final int ANIMATED_ITEMS_COUNT = 2;

	private Context context;
	private int lastAnimatedPosition = -1;
	private int itemsCount = 0;

	private final Map<Integer, Integer> likesCount = new HashMap<>();

	private final Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();

	private final ArrayList<Integer> likedPositions = new ArrayList<>();

	private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
	private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
	private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

	private OnFeedItemClickListener onFeedItemClickListener;

	public FeedAdapter(Context context) {
		this.context = context;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
		return new CellFeedViewHolder(view);
	}

	private void runEnterAnimation(View view, int position) {
		if (position >= ANIMATED_ITEMS_COUNT - 1) {
			return;
		}

		if (position > lastAnimatedPosition) {
			lastAnimatedPosition = position;
			view.setTranslationY(Util.getScreenHeight(context));
			view.animate()
					.translationY(0)
					.setInterpolator(new DecelerateInterpolator(3.f))
					.setDuration(700)
					.start();
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		runEnterAnimation(viewHolder.itemView, position);
		CellFeedViewHolder holder = (CellFeedViewHolder) viewHolder;
		if (position % 2 == 0) {
			holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
			holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
		} else {
			holder.ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
			holder.ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
		}
		likesCount.put(position, 123);
		updateLikesCounter(holder, false);
		updateHeartButton(holder, false);

		holder.btnComments.setOnClickListener(this);
		holder.btnComments.setTag(position);

		holder.btnMore.setOnClickListener(this);
		holder.btnMore.setTag(position);

		holder.btnLike.setOnClickListener(this);
		holder.btnLike.setTag(holder);

		holder.ivFeedCenter.setOnClickListener(this);
		holder.ivFeedCenter.setTag(holder);

		if (likeAnimations.containsKey(holder)) {
			likeAnimations.get(holder).cancel();
		}
		resetLikeAnimationState(holder);
	}

	@Override
	public int getItemCount() {
		return itemsCount;
	}

	@Override
	public void onClick(View v) {
		CellFeedViewHolder holder;
		switch (v.getId()){
			case R.id.btnComments:
				if (onFeedItemClickListener != null) {
					onFeedItemClickListener.onCommentsClick(v, (Integer) v.getTag());
				}
				break;

			case R.id.btnMore:
				if (onFeedItemClickListener != null) {
					onFeedItemClickListener.onMoreClick(v, (Integer) v.getTag());
				}
				break;

			case R.id.btnLike:
				holder = (CellFeedViewHolder) v.getTag();
				if (!likedPositions.contains(holder.getPosition())) {
					likedPositions.add(holder.getPosition());
					updateLikesCounter(holder, true);
					updateHeartButton(holder, true);
				}
				break;

			case R.id.ivFeedCenter:
				holder = (CellFeedViewHolder) v.getTag();
				if (!likedPositions.contains(holder.getPosition())) {
					likedPositions.add(holder.getPosition());
					updateLikesCounter(holder, true);
					animatePhotoLike(holder);
					updateHeartButton(holder, false);
				}
		}
	}

	public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
		@InjectView(R.id.ivFeedCenter)
		ImageView ivFeedCenter;
		@InjectView(R.id.ivFeedBottom)
		ImageView ivFeedBottom;
		@InjectView(R.id.btnComments)
		ImageButton btnComments;
		@InjectView(R.id.btnLike)
		ImageButton btnLike;
		@InjectView(R.id.btnMore)
		ImageButton btnMore;
		@InjectView(R.id.vBgLike)
		View vBgLike;
		@InjectView(R.id.ivLike)
		ImageView ivLike;
		@InjectView(R.id.tsLikesCounter)
		TextSwitcher tsLikesCounter;
		@InjectView(R.id.ivUserProfile)
		ImageView ivUserProfile;
		@InjectView(R.id.vImageRoot)
		FrameLayout vImageRoot;
		@InjectView(R.id.tv_like_counts)
		TextView tv_like_counts;

//		SendingProgressView vSendingProgress;
		View vProgressBg;

		public CellFeedViewHolder(View view) {
			super(view);
			ButterKnife.inject(this, view);
		}

	}

	public void updateItems() {
		itemsCount = 10;
		notifyDataSetChanged();
	}

	public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
		this.onFeedItemClickListener = onFeedItemClickListener;
	}

	public interface OnFeedItemClickListener {
		public void onCommentsClick(View v, int position);
		public void onMoreClick(View v,int tag);
	}

	private void updateLikesCounter(CellFeedViewHolder holder, boolean animated) {

		int currentLikesCount = likesCount.get(holder.getPosition()) + 1;
		String likesCountText = context.getResources().getQuantityString(
				R.plurals.likes_count, currentLikesCount, currentLikesCount
		);

		if (animated) {
			holder.tsLikesCounter.setText(likesCountText);
		} else {
			holder.tsLikesCounter.setCurrentText(likesCountText);
		}

		likesCount.put(holder.getPosition(), currentLikesCount);
	}

	private void updateHeartButton(final CellFeedViewHolder holder, boolean animated) {
		if (animated) {
			if (!likeAnimations.containsKey(holder)) {
				AnimatorSet animatorSet = new AnimatorSet();
				likeAnimations.put(holder, animatorSet);

				ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.btnLike, "rotation", 0f, 360f);
				rotationAnim.setDuration(300);
				rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

				ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.btnLike, "scaleX", 0.2f, 1f);
				bounceAnimX.setDuration(300);
				bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

				ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.btnLike, "scaleY", 0.2f, 1f);
				bounceAnimY.setDuration(300);
				bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
				bounceAnimY.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animation) {
						holder.btnLike.setImageResource(R.drawable.ic_heart_red);
					}
				});

				animatorSet.play(rotationAnim);
				animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

				animatorSet.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						resetLikeAnimationState(holder);
					}
				});

				animatorSet.start();
			}
		} else {
			if (likedPositions.contains(holder.getPosition())) {
				holder.btnLike.setImageResource(R.drawable.ic_heart_red);
			} else {
				holder.btnLike.setImageResource(R.drawable.ic_heart_outline_grey);
			}
		}
	}

	private void resetLikeAnimationState(CellFeedViewHolder holder) {
		likeAnimations.remove(holder);
		holder.vBgLike.setVisibility(View.GONE);
		holder.ivLike.setVisibility(View.GONE);
	}

	private void animatePhotoLike(final CellFeedViewHolder holder) {
		if (!likeAnimations.containsKey(holder)) {
			holder.vBgLike.setVisibility(View.VISIBLE);
			holder.ivLike.setVisibility(View.VISIBLE);

			holder.vBgLike.setScaleY(0.1f);
			holder.vBgLike.setScaleX(0.1f);
			holder.vBgLike.setAlpha(1f);
			holder.ivLike.setScaleY(0.1f);
			holder.ivLike.setScaleX(0.1f);

			AnimatorSet animatorSet = new AnimatorSet();
			likeAnimations.put(holder, animatorSet);

			ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
			bgScaleYAnim.setDuration(200);
			bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
			ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
			bgScaleXAnim.setDuration(200);
			bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
			ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
			bgAlphaAnim.setDuration(200);
			bgAlphaAnim.setStartDelay(150);
			bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

			ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
			imgScaleUpYAnim.setDuration(300);
			imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
			ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
			imgScaleUpXAnim.setDuration(300);
			imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

			ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
			imgScaleDownYAnim.setDuration(300);
			imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
			ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
			imgScaleDownXAnim.setDuration(300);
			imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

			animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
			animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					resetLikeAnimationState(holder);
				}
			});
			animatorSet.start();
		}
	}
}
