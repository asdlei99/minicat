package com.fanfou.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.fanfou.app.api.User;
import com.fanfou.app.cache.CacheManager;
import com.fanfou.app.cache.IImageLoader;
import com.fanfou.app.dialog.ConfirmDialog;
import com.fanfou.app.service.Constants;
import com.fanfou.app.service.FanFouService;
import com.fanfou.app.ui.ActionBar;
import com.fanfou.app.ui.ActionBar.AbstractAction;
import com.fanfou.app.ui.ActionManager;
import com.fanfou.app.util.DateTimeHelper;
import com.fanfou.app.util.OptionHelper;
import com.fanfou.app.util.StringHelper;
import com.fanfou.app.util.Utils;

/**
 * @author mcxiaoke
 * @version 1.0 2011.07.18
 * @version 1.1 2011.10.25
 * @version 1.2 2011.10.27
 * @version 1.3 2011.10.28
 * @version 1.4 2011.10.29
 * @version 1.5 2011.11.11
 * @version 1.6 2011.11.16
 * @version 1.7 2011.11.18
 * @version 1.8 2011.11.22
 * @version 2.0 2011.12.19
 * 
 */
public class ProfilePage extends BaseActivity {

	private ScrollView mScrollView;
	private View mEmptyView;

	private ActionBar mActionBar;

	private RelativeLayout mHeader;
	private ImageView mHead;
	private TextView mName;

	private ImageView mProtected;
	private TextView mRelationship;

	private LinearLayout mActions;
	private ImageView mReplyAction;
	private ImageView mMessageAction;
	private ImageView mFollowAction;

	private TextView mDescription;

	private ViewGroup mStatusesView;
	private TextView mStatusesTitle;
	private TextView mStatusesInfo;

	private ViewGroup mFavoritesView;
	private TextView mFavoritesTitle;
	private TextView mFavoritesInfo;

	private ViewGroup mFriendsView;
	private TextView mFriendsTitle;
	private TextView mFriendsInfo;

	private ViewGroup mFollowersView;
	private TextView mFollowersTitle;
	private TextView mFollowersInfo;

	private TextView mExtraInfo;

	private String userId;

	private User user;

	private Handler mHandler;
	private IImageLoader mLoader;

	private boolean isInitialized = false;
	private boolean noPermission = false;
	private boolean isBusy = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		parseIntent();
		initialize();
		setLayout();
		initCheckState();
	}

	private void parseIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();
		if (action == null) {
			userId = intent.getStringExtra(Constants.EXTRA_ID);
			user = (User) intent.getParcelableExtra(Constants.EXTRA_DATA);
			if (user != null) {
				userId = user.id;
			}
		} else if (action.equals(Intent.ACTION_VIEW)) {
			Uri data = intent.getData();
			if (data != null) {
				userId = data.getLastPathSegment();
			}
		}
		if (user == null && userId != null) {
			user = CacheManager.getUser(this, userId);
		}

		if (user != null) {
			userId = user.id;
		}

		if (App.getUserId().equals(userId)) {
			ActionManager.doMyProfile(this);
			finish();
		}

	}

	private void initialize() {
		mHandler = new Handler();
		mLoader = App.getImageLoader();
	}

	private void setLayout() {
		setContentView(R.layout.profile);

		// View root=findViewById(R.id.root);
		// ThemeHelper.setBackgroundColor(root);

		setActionBar();

		mEmptyView = findViewById(R.id.empty);
		mScrollView = (ScrollView) findViewById(R.id.user_profile);

		mHeader = (RelativeLayout) findViewById(R.id.user_headview);
		mHead = (ImageView) findViewById(R.id.user_head);
		mName = (TextView) findViewById(R.id.user_name);
		TextPaint tp = mName.getPaint();
		tp.setFakeBoldText(true);

		mExtraInfo = (TextView) findViewById(R.id.user_extrainfo);

		mProtected = (ImageView) findViewById(R.id.user_protected);

		mRelationship = (TextView) findViewById(R.id.user_relationship);

		mDescription = (TextView) findViewById(R.id.user_description);

		mActions = (LinearLayout) findViewById(R.id.user_actionview);
		mReplyAction = (ImageView) findViewById(R.id.user_action_reply);
		mMessageAction = (ImageView) findViewById(R.id.user_action_message);
		mFollowAction = (ImageView) findViewById(R.id.user_action_follow);

		mStatusesView = (ViewGroup) findViewById(R.id.user_statuses_view);
		mStatusesTitle = (TextView) findViewById(R.id.user_statuses_title);
		mStatusesInfo = (TextView) findViewById(R.id.user_statuses);

		mFavoritesView = (ViewGroup) findViewById(R.id.user_favorites_view);
		mFavoritesTitle = (TextView) findViewById(R.id.user_favorites_title);
		mFavoritesInfo = (TextView) findViewById(R.id.user_favorites);

		mFriendsView = (ViewGroup) findViewById(R.id.user_friends_view);
		mFriendsTitle = (TextView) findViewById(R.id.user_friends_title);
		mFriendsInfo = (TextView) findViewById(R.id.user_friends);

		mFollowersView = (ViewGroup) findViewById(R.id.user_followers_view);
		mFollowersTitle = (TextView) findViewById(R.id.user_followers_title);
		mFollowersInfo = (TextView) findViewById(R.id.user_followers);

		mStatusesView.setOnClickListener(this);
		mFavoritesView.setOnClickListener(this);
		mFriendsView.setOnClickListener(this);
		mFollowersView.setOnClickListener(this);

		mReplyAction.setOnClickListener(this);
		mMessageAction.setOnClickListener(this);
		mFollowAction.setOnClickListener(this);

		mScrollView.setVisibility(View.GONE);
	}

	/**
	 * 初始化和设置ActionBar
	 */
	private void setActionBar() {
		mActionBar = (ActionBar) findViewById(R.id.actionbar);
		mActionBar.setTitle("个人空间");
		mActionBar.setRightAction(new WriteAction());
	}

	private class WriteAction extends AbstractAction {

		public WriteAction() {
			super(R.drawable.i_write);
		}

		@Override
		public void performAction(View view) {
			ActionManager.doWrite(mContext);

		}
	}

	protected void initCheckState() {
		if (user != null) {
			showContent();
			updateUI();
		} else {
			doRefresh();
			mEmptyView.setVisibility(View.VISIBLE);
		}
	}

	private void showContent() {
		if (App.DEBUG)
			log("showContent()");
		isInitialized = true;
		mEmptyView.setVisibility(View.GONE);
		mScrollView.setVisibility(View.VISIBLE);

	}

	private void updateUI() {
		if (user == null) {
			return;
		}
		noPermission = !user.following && user.protect;

		if (App.DEBUG)
			log("updateUI user.name=" + user.screenName);

		boolean textMode = OptionHelper.readBoolean(R.string.option_text_mode,
				false);
		if (textMode) {
			mHead.setVisibility(View.GONE);
		} else {
			mHead.setTag(user.profileImageUrl);
			mLoader.displayImage(user.profileImageUrl, mHead,
					R.drawable.default_head);
		}

		mName.setText(user.screenName);

		String prefix;

		if (user.gender.equals("男")) {
			prefix = "他";
		} else if (user.gender.equals("女")) {
			prefix = "她";
		} else {
			prefix = "TA";
		}

		mActionBar.setTitle(user.screenName);
		mStatusesTitle.setText(prefix + "的消息");
		mFavoritesTitle.setText(prefix + "的收藏");
		mFriendsTitle.setText(prefix + "关注的人");
		mFollowersTitle.setText("关注" + prefix + "的人");

		mStatusesInfo.setText("" + user.statusesCount);
		mFavoritesInfo.setText("" + user.favouritesCount);
		mFriendsInfo.setText("" + user.friendsCount);
		mFollowersInfo.setText("" + user.followersCount);
		if (App.DEBUG)
			log("updateUI user.description=" + user.description);

		if (StringHelper.isEmpty(user.description)) {
			mDescription.setText("这家伙什么也没留下");
			mDescription.setGravity(Gravity.CENTER);
		} else {
			mDescription.setText(user.description);
		}

		mProtected.setVisibility(user.protect ? View.VISIBLE : View.GONE);

		setExtraInfo(user);
		updateFollowButton(user.following);

		if (!noPermission) {
			doFetchRelationshipInfo();
		}
	}

	private void setExtraInfo(User u) {
		if (u == null) {
			mExtraInfo.setVisibility(View.GONE);
			return;
		}

		StringBuffer sb = new StringBuffer();

		if (!StringHelper.isEmpty(user.gender)) {
			sb.append("性别：").append(user.gender).append("\n");
		}
		if (!StringHelper.isEmpty(user.birthday)) {
			sb.append("生日：").append(user.birthday).append("\n");
		}
		if (!StringHelper.isEmpty(user.location)) {
			sb.append("位置：").append(user.location).append("\n");
		}

		if (!StringHelper.isEmpty(user.url)) {
			sb.append("网站：").append(user.url).append("\n");
		}

		sb.append("注册时间：")
				.append(DateTimeHelper.formatDateOnly(user.createdAt));

		mExtraInfo.setText(sb.toString());

	}

	private void doRefresh() {
		FanFouService.doProfile(this, userId, new ResultHandler());
		if (isInitialized) {
			startRefreshAnimation();
		}
	}

	private void doFetchRelationshipInfo() {
		FanFouService.doFriendshipsExists(this, user.id, App.getUserId(),
				new ResultHandler());
	}

	private void updateFollowButton(boolean following) {
		mFollowAction.setImageResource(following ? R.drawable.btn_unfollow
				: R.drawable.btn_follow);
	}

	private void updateRelationshipState(boolean follow) {
		mRelationship.setVisibility(View.VISIBLE);
		mRelationship.setText(follow ? "(此用户正在关注你)" : "(此用户没有关注你)");
	}

	private void doFollow() {
		if (user == null || user.isNull()) {
			return;
		}

		if (user.following) {
			final ConfirmDialog dialog = new ConfirmDialog(this, "取消关注",
					"要取消关注" + user.screenName + "吗？");
			dialog.setClickListener(new ConfirmDialog.AbstractClickHandler() {

				@Override
				public void onButton1Click() {
					updateFollowButton(false);
					FanFouService.doFollow(mContext, user, new ResultHandler());
				}
			});

			dialog.show();
		} else {
			updateFollowButton(true);
			FanFouService.doFollow(mContext, user, new ResultHandler());
		}

	}

	@Override
	public void onClick(View v) {
		if (user == null || user.isNull()) {
			return;
		}
		switch (v.getId()) {
		case R.id.user_action_reply:
			ActionManager.doWrite(this, "@" + user.screenName + " ");
			break;
		case R.id.user_action_message:
			ActionManager.doMessage(this, user);
			break;
		case R.id.user_action_follow:
			doFollow();
			break;
		case R.id.user_statuses_view:
			if (hasPermission()) {
				ActionManager.doShowTimeline(this, user);
			}
			break;
		case R.id.user_favorites_view:
			if (hasPermission()) {
				ActionManager.doShowFavorites(this, user);
			}
			break;
		case R.id.user_friends_view:
			if (hasPermission()) {
				ActionManager.doShowFriends(this, user);
			}
			break;
		case R.id.user_followers_view:
			if (hasPermission()) {
				ActionManager.doShowFollowers(this, user);
			}
			break;
		case R.id.user_location_view:
			break;
		case R.id.user_site_view:
			break;
		default:
			break;
		}

	}

	@Override
	public void onRefreshClick() {
		if (isBusy) {
			return;
		}
		doRefresh();
	}

	private synchronized void setBusy(boolean busy) {
		isBusy = busy;
	}

	@Override
	protected void startRefreshAnimation() {
		setBusy(true);
		mActionBar.startAnimation();
	}

	@Override
	protected void stopRefreshAnimation() {
		setBusy(false);
		mActionBar.stopAnimation();
	}

	private boolean hasPermission() {
		if (noPermission) {
			Utils.notify(this, "你没有通过这个用户的验证");
			return false;
		}
		return true;
	}

	private class ResultHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int type = msg.arg1;
			Bundle bundle = msg.getData();
			switch (msg.what) {
			case Constants.RESULT_SUCCESS:
				if (!isInitialized) {
					showContent();
				}
				if (bundle != null) {
					if (App.DEBUG)
						log("result ok, update ui");
					User result = (User) bundle
							.getParcelable(Constants.EXTRA_DATA);
					if (result != null) {
						user = result;
					}
					if (type == Constants.TYPE_FRIENDSHIPS_EXISTS) {
						boolean follow = bundle
								.getBoolean(Constants.EXTRA_BOOLEAN);
						if (App.DEBUG)
							log("user relationship result=" + follow);
						updateRelationshipState(follow);
					} else if (type == Constants.TYPE_USERS_SHOW) {
						if (App.DEBUG)
							log("show result=" + user.id);
						if (isInitialized) {
							stopRefreshAnimation();
						}
						updateUI();

					} else if (type == Constants.TYPE_FRIENDSHIPS_CREATE
							|| type == Constants.TYPE_FRIENDSHIPS_DESTROY) {
						if (App.DEBUG)
							log("user.following=" + user.following);
						updateFollowButton(user.following);
						Utils.notify(mContext, user.following ? "关注成功"
								: "取消关注成功");
					}
				}
				break;
			case Constants.RESULT_ERROR:
				if (App.DEBUG)
					log("result error");
				if (!isInitialized) {
					mEmptyView.setVisibility(View.GONE);
				}
				if (type == Constants.TYPE_FRIENDSHIPS_EXISTS) {
					return;
				} else if (type == Constants.TYPE_USERS_SHOW) {
					stopRefreshAnimation();
				} else if (type == Constants.TYPE_FRIENDSHIPS_CREATE
						|| type == Constants.TYPE_FRIENDSHIPS_DESTROY) {
					updateFollowButton(user.following);
				}

				String errorMessage = bundle.getString(Constants.EXTRA_ERROR);
				Utils.notify(mContext, errorMessage);
				break;
			default:
				break;
			}
		}

	}

	private static final String tag = ProfilePage.class.getSimpleName();

	private void log(String message) {
		Log.d(tag, message);
	}

}
