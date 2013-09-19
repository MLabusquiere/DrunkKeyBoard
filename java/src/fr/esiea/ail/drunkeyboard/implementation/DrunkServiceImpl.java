package fr.esiea.ail.drunkeyboard.implementation;

import android.inputmethodservice.InputMethodService;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;
import fr.esiea.ail.drunkeyboard.IDrunkService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
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
public class DrunkServiceImpl implements IDrunkService {
    private final static Logger LOGGER = Logger.getLogger("SoftKey.DrunkService");
    private static final int BUFFER_SIZE = 30;
    private static final int SIZE_MAX = 15;
    private static final long TIME_MAX_MILLISECONDS = 6000;
    private static final Integer MAX_FREQUENCY = 5;
    private static final String WORD_KEY ="not drunk";
    private boolean inPreview=false;
    private String text;
    private long lastCurrentTime = 0;
    private final View keyboard;
    private String buffer = "";
    private boolean drunk = false;
    //private Camera mServiceCamera = Camera.open(CAMERA_FACING_BACK);
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private boolean mRecordingStatus;
    private final InputMethodService inputService;


    public DrunkServiceImpl(final View keyboard,final InputMethodService inputService) {
        this.keyboard     = keyboard;
        this.inputService = inputService;
    }

    @Override
    public boolean isUserDrunk(InputConnection currentInputConnection) {
        LOGGER.log(Level.INFO, "In isUserDrunk");
        text = currentInputConnection.getTextBeforeCursor(BUFFER_SIZE, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE).toString();
        boolean tooLong = isTooLong(text);
        boolean notAWord = isNotAWord(text);
        boolean random = isRandom(text);
        boolean tooSlow = isTooSlow(text);
        LOGGER.log(Level.INFO," "+  tooLong +" " + random + " " +tooSlow);
        return tooLong || notAWord || random || tooSlow || drunk;

    }

    private boolean isTooSlow(String text) {

        LOGGER.log(Level.INFO,"In isTooSlow");

        boolean result = false;

        if( 0 == lastCurrentTime)
            return false;  //Initialisation first call

        long currentTime = System.currentTimeMillis();

        if(TIME_MAX_MILLISECONDS < lastCurrentTime - currentTime)
            result = true;

        lastCurrentTime = currentTime;
        return result;

    }

    private boolean isRandom(String text) {
        LOGGER.log(Level.INFO,"In isRandom");

        HashMap<Character,Integer> map = new HashMap<Character,Integer>();
        String[] split = text.split(" ");
        String s = split[split.length - 1];
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(map.containsKey(c)){
                map.put(c, new Integer(map.get(c) + 1));
            }else{
                map.put(c,1);
            }

        }

        for(Integer i:map.values()) {
            if(MAX_FREQUENCY < i)
                return true;
        }
        return false;
    }

    private boolean isNotAWord(String text) {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    private boolean isTooLong(String text) {
        LOGGER.log(Level.INFO,"In isTooLong");

        String[] split = text.split(" ");
        for(String s: split)    {
            if( SIZE_MAX < s.length() )
                return true;
        }
        return false;
    }

    @Override
    public void onDrunkKey(int primaryCode, int[] keyCodes) {

        this.buffer += "" + ((char)primaryCode);
        LOGGER.log(Level.INFO,"buffer : "+buffer);
        if(!WORD_KEY.startsWith(this.buffer)) {
            this.buffer = "";

            Toast.makeText(keyboard.getContext(), "Try again, noob !", Toast.LENGTH_SHORT).show();
            return;
        }

        drunk = this.buffer.length() != WORD_KEY.length();
        LOGGER.log(Level.INFO,drunk + " " + (this.buffer.length() != WORD_KEY.length()) );


    }

    @Override
    public void execute(InputConnection currentInputConnection) {

        removeText(currentInputConnection);
        setStateDrunk();
        takePicture();
        Toast.makeText(keyboard.getContext(),"You are drunk type \""+WORD_KEY+"\" to enables keyboard",Toast.LENGTH_LONG).show();

    }

    private void setStateDrunk() {
        drunk = true;
    }

    private void takePicture()  {
   /*     Camera camera = Camera.open(0);
        Camera.Parameters parameters = camera.getParameters();
        camera.startPreview();
        Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                new SavePhotoTask().execute(data);
                camera.startPreview();
                inPreview=true;
            }
        };
        camera.takePicture(null, null, pictureCallback);
        camera.release()*/;

      /*  try {
            startMediaRecording();
            Thread.sleep(10000);
            stopMediaRecorder();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }*/
    }

/*    public boolean startMediaRecording() throws IOException {
        Camera.Parameters params = mServiceCamera.getParameters();
        mServiceCamera.setParameters(params);
        Camera.Parameters p = mServiceCamera.getParameters();

        final List<Camera.Size> listSize = p.getSupportedPreviewSizes();
        Camera.Size mPreviewSize = listSize.get(2);
        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
        p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
        mServiceCamera.setParameters(p);

        try {
            mServiceCamera.setPreviewDisplay(mSurfaceHolder);
            mServiceCamera.startPreview();
        }
        catch (IOException e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
        }

        mServiceCamera.unlock();

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setCamera(mServiceCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setOutputFile("/sdcard/filenamevideo.mp4");
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

        mMediaRecorder.prepare();
        mMediaRecorder.start();

        mRecordingStatus = true;

        return true;

    }

    public void stopMediaRecorder() throws IOException {
        mServiceCamera.reconnect();

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        mServiceCamera.stopPreview();
        mMediaRecorder.release();

        mServiceCamera.release();
        mServiceCamera = null;
    }
    */

    private void removeText(InputConnection currentInputConnection) {
        int length = currentInputConnection.getTextBeforeCursor(1000, InputConnection.GET_TEXT_WITH_STYLES).length();
        for(int i=0; i < length;i++)    {
            inputService.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
    }

    class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... jpeg) {
            File photo=new File(Environment.getExternalStorageDirectory(),
                    "photo.jpg");

            if (photo.exists()) {
                photo.delete();
            }

            try {
                FileOutputStream fos=new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);
                fos.close();
            }
            catch (java.io.IOException e) {
                LOGGER.info("Exception in photoCallback "+ e);
            }

            return(null);
        }
    }
}
