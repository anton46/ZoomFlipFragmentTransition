package id.co.anton19.zoomflip;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class ZoomFlip {
	/*
	 * Author : Anton Nurdin Tuhadiansyah
	 * Email  : anton.work19@gmail.com
	 * Powered by : developer.google.com
	 * Reference : http://developer.android.com/training/animation/index.html 
	 */
	private Animator mCurrentAnimator;
	private View mMainContainer;
	private View mOverlayLayout;
	private View mParentLayout;
	private View mThumb;

	private final Rect startBounds = new Rect();
	private final Rect finalBounds = new Rect();
	private final Point globalOffset = new Point();
	private float startScaleFinal;

	private OnClickListener zoomOutClickListener;
	private ShowingBackListener mShowingBackListener;
	
	private boolean mIsShowingBack = false;

	public ZoomFlip(View parentLayout, View mainContainer, View overlayLayout) {
		this.mMainContainer = mainContainer;
		this.mOverlayLayout = overlayLayout;
		this.mParentLayout = parentLayout;
	}

	@SuppressLint("NewApi")
	private void moveToThumb(Rect startBounds, float startScaleFinal) {
		mIsShowingBack = false;
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		AnimatorSet set = new AnimatorSet();
		set.play(ObjectAnimator.ofFloat(mMainContainer, "x", startBounds.left))
				.with(ObjectAnimator.ofFloat(mMainContainer, "y",
						startBounds.top))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleX",
						startScaleFinal))
				.with(ObjectAnimator.ofFloat(mMainContainer, "scaleY",
						startScaleFinal))
				.with(ObjectAnimator.ofFloat(mOverlayLayout, "alpha", 1f, 0f));
		set.setDuration(800);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mThumb.setAlpha(1f);
				mMainContainer.setVisibility(View.GONE);
				mOverlayLayout.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mThumb.setAlpha(1f);
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
		mIsShowingBack = true;
		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(mMainContainer, "x", startBounds.left,
						finalBounds.left))
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

	public void zoomImageFromThumb(View vThumb) {
		mThumb = vThumb;
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}
		
		mThumb.getGlobalVisibleRect(startBounds);
		mParentLayout.getGlobalVisibleRect(finalBounds, globalOffset);
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
		
		mThumb.setAlpha(0f);
		mOverlayLayout.setVisibility(View.VISIBLE);
		mMainContainer.setVisibility(View.VISIBLE);
		mMainContainer.setPivotX(0f);
		mMainContainer.setPivotY(0f);

		moveToCenter(startBounds, finalBounds, startScale);

		startScaleFinal = startScale;
		zoomOutClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if (mIsShowingBack) {
					mShowingBackListener.onShowingBackClick();
				}
			}
		};
		mOverlayLayout.setOnClickListener(zoomOutClickListener);
	}

	public void moveToThumb() {
		moveToThumb(startBounds, startScaleFinal);
	}

	public ShowingBackListener getShowingBackListener() {
		return mShowingBackListener;
	}

	public void setShowingBackListener(ShowingBackListener mShowingBackListener) {
		this.mShowingBackListener = mShowingBackListener;
	}

}
