package mjn.videoview;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Formatter;
import java.util.Locale;

import mjn.videoview.widget.media.MJNBaseVideoView;

/**
 * Created by mengjingnan on 2017/2/16.
 */

public class MJNVideoView extends MJNBaseVideoView implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    /**
     * the ui of video
     */
    protected View mTopBar;
    protected View mBottomBar;
    protected View mCompletedView;//完成
    protected ImageView mCoverView;//封面
    protected View mMaskView;//遮罩
    protected View mBackIv;
    protected View mReplayView;
    protected View mShareView;
    protected TextView mTitleTv;
    protected TextView mPlayedDurationTv;
    protected TextView mTotalDurationTv;
    protected SeekBar mSeekBar;
    protected ImageButton mRotateBtn;
    protected ProgressBar mLoadingProgressBar;
    protected ImageButton mPlayBtn;
    protected ProgressBar mBottomPb;
    protected TextView mErrorTv;


    protected AudioManager mAudioManager; //音频焦点的监听

    /**
     * the touch of video
     */
    protected int mScreenWidth; //屏幕宽度

    protected int mScreenHeight; //屏幕高度

    protected int mThreshold = 0; //手势偏差值

    protected float mBrightnessData = -1; //亮度

    protected int mDownPosition; //手指放下的位置

    protected int mGestureDownVolume; //手势调节音量的大小

    protected boolean mTouchingProgressBar = false;
    protected float mDownX;//触摸的X

    protected float mDownY; //触摸的Y

    protected float mLastMoveX;
    protected float mMoveY;

    protected boolean mIsTouchWiget = false;

    protected boolean mChangeVolume = false;//是否改变音量

    protected boolean mChangePosition = false;//是否改变播放进度

    protected boolean mShowVKey = false; //触摸显示虚拟按键

    protected boolean mBrightness = false;//是否改变亮度

    protected boolean mFirstTouch = false;//是否首次触摸

    protected boolean mManualTriggerPlaying = false;//是否手动触发播放

    protected int mSeekTimePosition; //手动改变滑动的位置

    protected int mSeekEndOffset; //手动滑动的起始偏移位置

    protected int mSystemUiVisibility;


    public MJNVideoView(Context context) {
        super(context);
        initView();
    }

    public MJNVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public MJNVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MJNVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mTopBar = findViewById(R.id.top_bar);
        mBottomBar = findViewById(R.id.video_bottom_media_controller);
        mCompletedView = findViewById(R.id.video_view_play_complete_panel);
        //这个要优化成可配置的
        mCoverView = (ImageView) findViewById(R.id.video_cover);
        mBackIv = findViewById(R.id.video_back);
        mPlayedDurationTv = (TextView) findViewById(R.id.video_bottom_played_duration_tv);
        mTotalDurationTv = (TextView) findViewById(R.id.video_bottom_played_total_duration_tv);
        mSeekBar = (SeekBar) findViewById(R.id.video_bottom_played_duration_sb);
        mRotateBtn = (ImageButton) findViewById(R.id.video_bottom_rotate_btn);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.video_view_loading_pb);
        mBottomPb = (ProgressBar) findViewById(R.id.video_bottom_pb);
        mPlayBtn = (ImageButton) findViewById(R.id.video_play_btn);
        mErrorTv = (TextView) findViewById(R.id.video_error);
        mReplayView = findViewById(R.id.video_view_play_complete_panel_replay_ll);
        mShareView = findViewById(R.id.video_view_play_complete_panel_share_ll);
        //点击事件
        mBackIv.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mRotateBtn.setOnClickListener(this);
        mErrorTv.setOnClickListener(this);
        mReplayView.setOnClickListener(this);
        mShareView.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        mSystemUiVisibility = ((Activity) getContext()).getWindow().getDecorView().getSystemUiVisibility();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.video_view;
    }

    @Override
    protected void hideController() {
        super.hideController();
        mPlayBtn.setVisibility(GONE);
        mTopBar.setVisibility(GONE);
        mBottomBar.setVisibility(GONE);
        mSeekBar.setVisibility(GONE);
        mCompletedView.setVisibility(GONE);
        mCoverView.setVisibility(GONE);

    }

    @Override
    protected void showController() {
        super.showController();
        mPlayBtn.setVisibility(VISIBLE);
        mTopBar.setVisibility(VISIBLE);
        mBottomBar.setVisibility(VISIBLE);
        mSeekBar.setVisibility(VISIBLE);
        mCoverView.setVisibility(VISIBLE);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (R.id.video_play_btn == id){
            if (STATE_PLAYING == mCurrentState){
                pause();
                mPlayBtn.setImageResource(R.drawable.video_btn_play);
            } else {
                start();
                mPlayBtn.setImageResource(R.drawable.video_btn_pause);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int duration = getDuration();
        int progress = seekBar.getProgress();
        long time = (long) (duration * (progress * 1.0f / 100));
        if (time > duration) {
            time = duration;
        }
        seekTo((int) time);
    }

    @Override
    protected void onBufferProgressUpdate(int percent) {


        if (percent > 94) percent = 100;
        mSeekBar.setSecondaryProgress(percent);

    }

    @Override
    protected void onProgressUpdate() {
        int position = getCurrentPosition();
        int duration = getDuration();
        int progress = position * 100 / (duration == 0 ? 1 : duration);
        mSeekBar.setProgress(progress);
        mTotalDurationTv.setText(positionOfTime(duration));
        if (position > 0) {
            mPlayedDurationTv.setText(positionOfTime(position));
        }
    }

    public static String positionOfTime(int timeMs) {
        if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    protected void onVideoComplete() {
        mCompletedView.setVisibility(VISIBLE);
        mPlayBtn.setVisibility(GONE);
        showController();
    }

    @Override
    protected void dismissProgressDialog() {

    }

    @Override
    protected void dismissVolumeDialog() {

    }

    @Override
    protected void dismissBrightnessDialog() {

    }

    @Override
    protected void showProgressDialog(float deltaX, int seekTimePosition, int totalTimeDuration) {

    }

}
