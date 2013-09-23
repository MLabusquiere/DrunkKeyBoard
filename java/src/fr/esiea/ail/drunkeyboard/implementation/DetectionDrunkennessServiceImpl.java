package fr.esiea.ail.drunkeyboard.implementation;

import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;
import fr.esiea.ail.drunkeyboard.IDetectionDrunkennessService;
import fr.esiea.ail.drunkeyboard.IDetectionDrunkennessService;
import org.pocketworkstation.pckeyboard.LatinIME;

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
public class DetectionDrunkennessServiceImpl implements IDetectionDrunkennessService {
    private final static Logger LOGGER = Logger.getLogger("SoftKey.DrunkService");
    private static final int BUFFER_SIZE = 30;
    private static final int SIZE_MAX = 15;
    private static final long TIME_MAX_MILLISECONDS = 6000;
    private static final Integer MAX_FREQUENCY = 5;
    private static final String WORD_KEY ="not drunk";
    private boolean inPreview=false;
    private String text;
    private long lastCurrentTime = 0;
    private String buffer = "";
    private boolean drunk = false;
    //private Camera mServiceCamera = Camera.open(CAMERA_FACING_BACK);
    private SurfaceHolder mSurfaceHolder;
    private MediaRecorder mMediaRecorder;
    private boolean mRecordingStatus;
    private final LatinIME latinIME;


    public DetectionDrunkennessServiceImpl(final LatinIME inputService) {
        this.latinIME = inputService;
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

            Toast.makeText(latinIME.getApplicationContext(), "Try again, noob !", Toast.LENGTH_SHORT).show();
            return;
        }

        drunk = this.buffer.length() != WORD_KEY.length();
        LOGGER.log(Level.INFO,drunk + " " + (this.buffer.length() != WORD_KEY.length()) );


    }

    @Override
    public void execute(InputConnection currentInputConnection) {

        removeText(currentInputConnection);
        setStateDrunk();
        Toast.makeText(latinIME.getApplicationContext(),"You are drunk type \""+WORD_KEY+"\" to enables keyboard",Toast.LENGTH_LONG).show();

    }

    private void setStateDrunk() {
        drunk = true;
    }

    private void removeText(InputConnection currentInputConnection) {
        int length = currentInputConnection.getTextBeforeCursor(1000, InputConnection.GET_TEXT_WITH_STYLES).length();
        for(int i=0; i < length;i++)    {
            latinIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
    }


}
