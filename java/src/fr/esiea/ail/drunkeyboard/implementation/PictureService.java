package fr.esiea.ail.drunkeyboard.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class PictureService extends Activity {

	private static final Logger LOGGER = Logger.getLogger("SoftKey.PictureService");
	private static final String JPEG_FILE_PREFIX = "JPEG_";
	private static final String JPEG_FILE_SUFFIX = ".jpeg";
	private Context context = super.getBaseContext();

	private boolean pictureTaken = false;

	/**
	 * Take a picture
	 * 
	 * @return If the picture has been taken, or not.
	 */
	public boolean takeApicture()  {

		//if a picture was already taken, do not take a new picture.
		if(pictureTaken) {
			return false;
		}


		//We intend to take a picture, so we check if it is possible.
		final boolean intentAvailable = isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE);
		if(!intentAvailable)  {
			LOGGER.log(Level.SEVERE, "Impossible to open the camera capture intent");
			return false;
		}
		try {
			dispatchTakePictureIntent(1);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}

		this.pictureTaken = true;

		return true;

	}

	@Override
	protected void onStart() {

		super.onStart();
		
		takeApicture();

		//takePictureNoPreview(super.getBaseContext());
	}
	
	/**
	 * Create and dispatch the intent to launch the Camera App
	 * 
	 * @param actionCode a code for the activity to be created.
	 * @throws IOException
	 */
	private void dispatchTakePictureIntent(int actionCode) throws IOException {
		
		//The Intent to take a picture using the default Camera app.
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		//HACK : by default the Camera app launches itself with the rear camera, with this it launches directly with the front camera
		takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);

		File f = createImageFile(); //create a file for our picture
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f)); //Tells to the Camera App where to store the picture
		
		startActivityForResult(takePictureIntent, actionCode); //Launch a new Activity with our Intent : the Camera App.
	}


	/**
	 * Check if an Intent is available, given an action
	 * 
	 * @param action
	 * @return
	 */
	public boolean isIntentAvailable(String action) {

		final PackageManager packageManager = getBaseContext().getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp =
				new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

		File image = File.createTempFile(
				imageFileName,
				JPEG_FILE_SUFFIX,
				getAlbumDir()
				);
		return image;
	}

	
	/**
	 * @return the directory where the pictures are stored on the SD Card.
	 */
	private File getAlbumDir() {
		return  new File(Environment.getExternalStorageDirectory()+"/Pictures");
	}


	public void takePictureNoPreview(Context context){
		// open back facing camera by default
		Camera myCamera= getFrontFacingCamera();

		if(myCamera!=null){
			try{
				//set camera parameters if you want to
				//...

				// here, the unused surface view and holder

				SurfaceView dummy=new SurfaceView(context);

				myCamera.setPreviewDisplay(dummy.getHolder());    

				myCamera.startPreview(); 


				System.gc();

				myCamera.takePicture(new Camera.ShutterCallback() {

					@Override
					public void onShutter() {
						// TODO Auto-generated method stub

					}
				}, new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						// TODO Auto-generated method stub

					}
				}, new Camera.PictureCallback() {

					@Override
					public void onPictureTaken(byte[] data, Camera camera) {

						LOGGER.log(Level.SEVERE, "IT WORKS");

					}
				});

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				myCamera.release();
			}      

		}else{
			//booo, failed!
		}
	}


	//Selecting front facing camera.
	private Camera getFrontFacingCamera() {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
			Camera.getCameraInfo( camIdx, cameraInfo );
			if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
				try {
					cam = Camera.open( camIdx );
				} catch (RuntimeException e) {
					LOGGER.log(Level.SEVERE, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}

}
