package demo.binea.com.instagramwithmaterialdesign.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import demo.binea.com.instagramwithmaterialdesign.R;
import demo.binea.com.instagramwithmaterialdesign.Util;
import demo.binea.com.instagramwithmaterialdesign.Utils.DrawerLayoutInstaller;
import demo.binea.com.instagramwithmaterialdesign.adapter.FeedAdapter;
import demo.binea.com.instagramwithmaterialdesign.view.FeedContextMenu;
import demo.binea.com.instagramwithmaterialdesign.view.FeedContextMenuManager;
import demo.binea.com.instagramwithmaterialdesign.view.GlobalMenuView;


public class MainActivity extends ActionBarActivity implements FeedAdapter.OnFeedItemClickListener, FeedContextMenu.OnFeedContextMenuItemClickListener{

	@InjectView(R.id.toolbar)
	Toolbar toolbar;
	@InjectView(R.id.rvFeed)
	RecyclerView rvFeed;
	@InjectView(R.id.btnCreate)
	ImageButton btnCreate;
	@InjectView(R.id.ivLogo)
	ImageView ivLogo;

	private MenuItem inboxMenuItem;
	private DrawerLayout drawerLayout;
	private FeedAdapter feedAdapter;
	private boolean pendingIntroAnimation;

	private static final int ANIM_DURATION_FAB = 400;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

		if (savedInstanceState == null) {
			pendingIntroAnimation = true;
		}
		setupToolbar();
		setupDrawer();
		setupFeed();
	}

	private void setupToolbar() {
		setSupportActionBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.ic_menu_white);
	}

	private void setupFeed() {
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
			@Override
			protected int getExtraLayoutSpace(RecyclerView.State state) {
				return 300;
			}
		};
		rvFeed.setLayoutManager(linearLayoutManager);
		feedAdapter = new FeedAdapter(this);
		feedAdapter.setOnFeedItemClickListener(this);
		rvFeed.setAdapter(feedAdapter);
		rvFeed.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				FeedContextMenuManager.getInstance().hideContextMenu();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		inboxMenuItem = menu.findItem(R.id.action_inbox);
		inboxMenuItem.setActionView(R.layout.menu_item_view);
		if (pendingIntroAnimation) {
			pendingIntroAnimation = false;
			startIntroAnimation();
		}
		return true;
	}

	private static final int ANIM_DURATION_TOOLBAR = 300;

	private void startIntroAnimation() {
		btnCreate.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.btn_fab_size));

		int actionbarSize = Util.dpToPx(56);
		toolbar.setTranslationY(-actionbarSize);
		ivLogo.setTranslationY(-actionbarSize);
		inboxMenuItem.getActionView().setTranslationY(-actionbarSize);

		toolbar.animate()
				.translationY(0)
				.setDuration(ANIM_DURATION_TOOLBAR)
				.setStartDelay(300);
		ivLogo.animate()
				.translationY(0)
				.setDuration(ANIM_DURATION_TOOLBAR)
				.setStartDelay(400);
		inboxMenuItem.getActionView().animate()
				.translationY(0)
				.setDuration(ANIM_DURATION_TOOLBAR)
				.setStartDelay(500)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						startContentAnimation();
					}
				})
				.start();
	}

	private void startContentAnimation() {
		btnCreate.animate()
				.translationY(0)
				.setInterpolator(new OvershootInterpolator(1.f))
				.setStartDelay(300)
				.setDuration(ANIM_DURATION_FAB)
				.start();
		feedAdapter.updateItems();
	}

	@Override
	public void onCommentsClick(View v, int position) {
		final Intent intent = new Intent(this, CommentsActivity.class);

		//Get location on screen for tapped view
		int[] startingLocation = new int[2];
		v.getLocationOnScreen(startingLocation);
		intent.putExtra(CommentsActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);

		startActivity(intent);
		overridePendingTransition(0, 0);
	}

	@Override
	public void onMoreClick(View v, int tag) {
		FeedContextMenuManager.getInstance().toggleContextMenuFromView(v, tag, this);
	}

	@Override
	public void onProfileClick(View v, int position) {
		int[] startingLocation = new int[2];
		v.getLocationOnScreen(startingLocation);
		startingLocation[0] += v.getWidth() / 2;
		UserProfileActivity.startUserProfileFromLocation(startingLocation, this);
		overridePendingTransition(0, 0);
	}

	@Override
	public void onReportClick(int feedItem) {
		FeedContextMenuManager.getInstance().hideContextMenu();
	}

	@Override
	public void onSharePhotoClick(int feedItem) {
		FeedContextMenuManager.getInstance().hideContextMenu();
	}

	@Override
	public void onCopyShareUrlClick(int feedItem) {
		FeedContextMenuManager.getInstance().hideContextMenu();
	}

	@Override
	public void onCancelClick(int feedItem) {
		FeedContextMenuManager.getInstance().hideContextMenu();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		FeedContextMenuManager.getInstance().hideContextMenu();
		return super.dispatchTouchEvent(ev);
	}

	private void setupDrawer() {
		GlobalMenuView menuView = new GlobalMenuView(this);
//		menuView.setOnHeaderClickListener(this);

		drawerLayout = DrawerLayoutInstaller.from(this)
				.drawerRoot(R.layout.drawer_root)
				.drawerLeftView(menuView)
				.drawerLeftWidth(Util.dpToPx(300))
				.withNavigationIconToggler(toolbar)
				.build();
	}
}
