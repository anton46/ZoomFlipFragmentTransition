package id.co.anton19.zoomflip;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
/*
 * Author : Anton Nurdin Tuhadiansyah
 * Email  : anton.work19@gmail.com 
 */

public class ZoomFlipActivity extends Activity implements
		FragmentManager.OnBackStackChangedListener {

	private static final String FIRST_BUTTON = "first";
	private static final String SECOND_BUTTON = "second";
	private static String TYPE;
	
	private final Rect startBounds = new Rect();
	private final Rect finalBounds = new Rect();
	private final Point globalOffset = new Point();
	private float startScaleFinal;
	
	private Animator mCurrentAnimator;
	private Handler mHandler = new Handler();
	private boolean mShowingBack = false;
	private RelativeLayout mMainContainer;
	private RelativeLayout mOverlayLayout;
	private TouchHighlightImageButton mImageBtn;
	private TouchHighlightImageButton mImageBtn_2;
	
	private OnClickListener zoomOutClickListener;
	
	//fragment
	private CardBackFragment mBackFragment;
	private CardFrontFragment mFrontFragment;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zoom_flip);
		mBackFragment = new CardBackFragment();
		mFrontFragment = new CardFrontFragment();

		getFragmentManager().beginTransaction()
				.add(R.id.main, mFrontFragment).commit();

		mMainContainer = (RelativeLayout) findViewById(R.id.main);
		mMainContainer.setVisibility(View.GONE);

		mOverlayLayout = (RelativeLayout) findViewById(R.id.overlay);
		mImageBtn = (TouchHighlightImageButton) findViewById(R.id.thumb_button);
		mImageBtn_2 = (TouchHighlightImageButton) findViewById(R.id.thumb_button_2);

		mImageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				flipCard();
				TYPE = FIRST_BUTTON;
				mFrontFragment.setImage(R.drawable.arsenal);
				zoomImageFromThumb(mImageBtn);
			}
		});
		mImageBtn_2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flipCard();
				TYPE = SECOND_BUTTON;
				mFrontFragment.setImage(R.drawable.madrid);
				zoomImageFromThumb(mImageBtn_2);
			}
		});

		getFragmentManager().addOnBackStackChangedListener(this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void flipCard() {
		if (mShowingBack) {
			getFragmentManager().popBackStack();
			return;
		}
		mShowingBack = true;

		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.animator.card_flip_right_in,
						R.animator.card_flip_right_out,
						R.animator.card_flip_left_in,
						R.animator.card_flip_left_out)
				.replace(R.id.main, mBackFragment)
				.addToBackStack(null).commit();

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidateOptionsMenu();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mShowingBack){
			flipCard();
			moveToThumb(startBounds, startScaleFinal);
		}
		else {
			super.onBackPressed();
		}
	}

	@SuppressLint("NewApi")
	private void moveToThumb(Rect startBounds, float startScaleFinal) {
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(mMainContainer, "x", startBounds.left))
				.with(ObjectAnimator.ofFloat(mMainContainer, "y",startBounds.top))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleX",startScaleFinal))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleY",startScaleFinal))
				.with(ObjectAnimator.ofFloat(mOverlayLayout, "alpha", 1f, 0f));
		set.setDuration(800);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mMainContainer.setVisibility(View.GONE);
				mOverlayLayout.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mMainContainer.setVisibility(View.GONE);
				mOverlayLayout.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
	}

	@SuppressLint("NewApi")
	private void moveToCenter(Rect startBounds, Rect finalBounds,
			float startScale) {
		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(mMainContainer, "x",
						startBounds.left, finalBounds.left))
				.with(ObjectAnimator.ofFloat(mMainContainer, "y",
						startBounds.top, finalBounds.top))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleX",
						startScale, 1f))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleY",
						startScale, 1f))
				.with(ObjectAnimator.ofFloat(mOverlayLayout, "alpha", 0, 1f));

		set.setDuration(800);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
	}

	private void zoomImageFromThumb(View vButton) {
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		final RelativeLayout expandedImageView = (RelativeLayout) findViewById(R.id.main);

		vButton.getGlobalVisibleRect(startBounds);
		findViewById(R.id.container).getGlobalVisibleRect(finalBounds,
				globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds
				.width() / startBounds.height()) {
			startScale = (float) startBounds.height() / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - startBounds.width()) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			startScale = (float) startBounds.width() / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - startBounds.height()) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		mOverlayLayout.setVisibility(View.VISIBLE);
		expandedImageView.setVisibility(View.VISIBLE);
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		moveToCenter(startBounds, finalBounds, startScale);

		startScaleFinal = startScale;
		zoomOutClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mShowingBack) {
					flipCard();
					moveToThumb(startBounds, startScaleFinal);
				}
			}
		};
		
		//expandedImageView.setOnClickListener(zoomOutClickListener);
		mOverlayLayout.setOnClickListener(zoomOutClickListener);
	}
	

	@Override
	public void onBackStackChanged() {
		mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
		invalidateOptionsMenu();
	}

	public static class CardFrontFragment extends Fragment {
		private ImageView mFrontImage;
		
		public CardFrontFragment() {
			
		}
		
		public void setImage(int img) {
			mFrontImage.setImageResource(img);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.fragment_card_front, container,
					false);
			mFrontImage = (ImageView) v.findViewById(R.id.image_robot);
			mFrontImage.setImageResource(TYPE == FIRST_BUTTON ? R.drawable.arsenal : R.drawable.madrid);
			return v;
		}
	}

	public static class CardBackFragment extends Fragment {
		private ImageView mBackImage;
		private TextView mTextView;
		
		public CardBackFragment(){
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v  =  inflater.inflate(R.layout.fragment_card_back, container,
					false);
			mBackImage = (ImageView) v.findViewById(R.id.image_robot);
			mBackImage.setImageResource(TYPE == FIRST_BUTTON ? R.drawable.arsenal : R.drawable.madrid);
			mTextView = (TextView) v.findViewById(R.id.text);
			mTextView.setText(TYPE == FIRST_BUTTON ? "Arsenal" : "Real Madrid");
			return v;
		}
	}
}
