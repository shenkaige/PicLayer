package com.phodev.piclayer;

import com.phodev.android.piclayer.R;
import com.phodev.piclayer.core.BackgroundLayer;
import com.phodev.piclayer.core.FrameLayer;
import com.phodev.piclayer.core.LayerView;
import com.phodev.piclayer.core.PictureLayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * 
 * @author sky
 *
 */
public class MainActivity extends Activity {
	private LayerView layerView;
	private ImageView resultImg;
	private FrameLayer frameLayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		layerView = (LayerView) findViewById(R.id.layer_view);
		resultImg = (ImageView) findViewById(R.id.result);
		// init background layer
		BackgroundLayer bgLayer = new BackgroundLayer();
		bgLayer.setLayerResouce(loadBitmap(R.drawable.test_photo));
		//
		frameLayer = new FrameLayer();
		frameLayer.setLayerResouce(loadBitmap(R.drawable.frame_s_jiao_pian));
		//
		PictureLayer picLayer = new PictureLayer(this);
		picLayer.setLayerResouce(loadBitmap(R.drawable.ic_launcher));
		// add
		layerView.setBackgroundLayer(bgLayer);
		layerView.addLayer(frameLayer);
		layerView.addLayer(picLayer);
		// remove
		// layerView.removeLayer(picLayer);//remove layer
		// meager
		// Bitmap result=layerView.mergeAllLayer(bgLayer);
		// test
		layerView.postDelayed(resultWatcher, 5000);

	}

	private Bitmap loadBitmap(int resId) {
		return BitmapFactory.decodeResource(getResources(), resId);
	}

	private Runnable resultWatcher = new Runnable() {

		@Override
		public void run() {
			layerView.postDelayed(this, 1000);
			Drawable d = resultImg.getDrawable();
			if (d instanceof BitmapDrawable) {
				resultImg.setImageDrawable(null);
				((BitmapDrawable) d).getBitmap().recycle();
			}
			resultImg.setImageBitmap(layerView.mergeAllLayer(frameLayer));
		}

	};

}
