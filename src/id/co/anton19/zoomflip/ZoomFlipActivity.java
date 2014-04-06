package id.co.anton19.zoomflip;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ZoomFlipActivity extends Activity implements
		FragmentManager.OnBackStackChangedListener {
	
	/*
	 * Author : Anton Nurdin Tuhadiansyah
	 * Email  : anton.work19@gmail.com
	 * Powered by : developer.google.com
	 * Reference : http://developer.android.com/training/animation/index.html 
	 */

	private static final String FIRST_BUTTON = "first";
	private static final String SECOND_BUTTON = "second";
	private static String TYPE;

	private boolean mShowingBack = false;
	
	//View
	private FrameLayout mParentLayout;
	private RelativeLayout mMainContainer;
	private RelativeLayout mOverlayLayout;
	private TouchHighlightImageButton mThumb1;
	private TouchHighlightImageButton mThumb2;
	
	//fragment
	private CardBackFragment mBackFragment;
	private CardFrontFragment mFrontFragment;
	
	//ZoomFLipLib
	private ZoomFlip mZoomFlip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zoom_flip);
		mBackFragment = new CardBackFragment();
		mFrontFragment = new CardFrontFragment();
		getFragmentManager().beginTransaction()
				.add(R.id.main, mFrontFragment).commit();
		
		
		mParentLayout = (FrameLayout) findViewById(R.id.container);
		mMainContainer = (RelativeLayout) findViewById(R.id.main);
		mMainContainer.setVisibility(View.GONE);

		mOverlayLayout = (RelativeLayout) findViewById(R.id.overlay);
		mThumb1 = (TouchHighlightImageButton) findViewById(R.id.thumb_button);
		mThumb2 = (TouchHighlightImageButton) findViewById(R.id.thumb_button_2);
		
		mZoomFlip = new ZoomFlip(mParentLayout, mMainContainer, mOverlayLayout);
		mZoomFlip.setShowingBackListener(new ShowingBackListener() {
			@Override
			public void onShowingBackClick() {
				flip();
			}
		});

		mThumb1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				flip();
				TYPE = FIRST_BUTTON;
				mFrontFragment.setImage(R.drawable.arsenal);
				mZoomFlip.zoomImageFromThumb(mThumb1);
			}
		});
		mThumb2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flip();
				TYPE = SECOND_BUTTON;
				mFrontFragment.setImage(R.drawable.madrid);
				mZoomFlip.zoomImageFromThumb(mThumb2);
			}
		});

		getFragmentManager().addOnBackStackChangedListener(this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void flip() {
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
	}

	@Override
	public void onBackPressed() {
		if (mShowingBack){
			flip();
			mZoomFlip.moveToThumb();
		}
		else {
			super.onBackPressed();
		}
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
