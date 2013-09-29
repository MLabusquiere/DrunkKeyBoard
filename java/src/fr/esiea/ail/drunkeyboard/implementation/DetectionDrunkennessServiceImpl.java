package fr.esiea.ail.drunkeyboard.implementation;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;
import fr.esiea.ail.drunkeyboard.IDetectionDrunkennessService;
import fr.esiea.ail.drunkeyboard.IDetectionDrunkennessService;
import org.pocketworkstation.pckeyboard.AutoDictionary;
import org.pocketworkstation.pckeyboard.LatinIME;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright (c) 2013 ESIEA M. Labusquiere D. Déïs N. Broquet
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
    /*
     * Size of the text which be used to apply drunkenness algorithm
     */
    private static final int BUFFER_SIZE = 30;
    /*
     * Size max of a word when you are not drunk
     */
    private static final int SIZE_MAX = 15;
    /*
     * Time max between two char
     */
    private static final long TIME_MAX_MILLISECONDS = 6000;
    /*
     * Frequency max than a char can appear in a word
     */
    private static final Integer MAX_FREQUENCY = 5;
    /*
     * Message than the user need to tape to unlock the keyboard
     */
    private static final String WORD_KEY ="not drunk";
    /*
     * Max word which is not belonged to the dictionary in a input
     */
    public static final int MAX_NOT_A_WORD_PERMIT = 5;
    /*
     * Buffer than keep the text entered by the user
     */
    private String text;
    /*
     * Last time than a user entered a char
     */
    private long lastCurrentTime = 0;
    /*
     * buffer use when the user is drunk to check if he's typing the right WORD_KEY
     */
    private String buffer = "";
    /*
     * Keep if the user is drunk
     */
    private boolean drunk = false;
    /*
     * The keyboard where this class need to get the context and the inputService
     */
    private final LatinIME latinIME;
    /*
     * The dictionary
     */
    private final AutoDictionary dictionary;
    /*
     * Count of words which is not in the dictionary
     */
    private int accOfwordWhishIsNotword = 0;
    /*
     * IoC helper
     */
    public DetectionDrunkennessServiceImpl(final LatinIME inputService,AutoDictionary dictionary) {
        this.latinIME = inputService;
        this.dictionary = dictionary;
    }

    /*
     * Check if the user is drunk
     */
    @Override
    public boolean isUserDrunk(InputConnection currentInputConnection) {

        //Get previous input
        text = currentInputConnection.getTextBeforeCursor(BUFFER_SIZE, InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE).toString();

        //Apply algorithm on the input
        boolean tooLong = isTooLong(text);
        boolean notAWord = isNotAWord(text);
        boolean random = isRandom(text);
        boolean tooSlow = isTooSlow(text);

        boolean isDrunk = tooLong || notAWord || random || tooSlow || drunk;

        LOGGER.log(Level.INFO, "Is User Drunk ?" + isDrunk);
        //Return if the use is drunk
        return isDrunk;

    }

    /*
     * Algorithm than calculating is the user is too slow when he is typing
     */
    private boolean isTooSlow(String text) {

        LOGGER.log(Level.INFO,"In isTooSlow");

        boolean result = false;

        if( 0 == lastCurrentTime)
            return false;  //Initialisation first call

        long currentTime = System.currentTimeMillis();

        //get the time between two char and compare it to the time max
        if(TIME_MAX_MILLISECONDS < lastCurrentTime - currentTime)
            result = true;

        lastCurrentTime = currentTime;

        //Return true if the use was too slow
        return result;

    }

    /*
     * Algorithm to detect if the user type random word
     */
    private boolean isRandom(String text) {

        LOGGER.log(Level.INFO,"In isRandom");

        //Innitialisation
        HashMap<Character,Integer> map = new HashMap<Character,Integer>();
        //Get Only the last word
        String[] split = text.split(" ");
        String s = split[split.length - 1];

        //Calculate the frequency of each char of the word
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(map.containsKey(c)){
                map.put(c, new Integer(map.get(c) + 1));
            }else{
                map.put(c,1);
            }

        }

        //if one frequency is higher than MAX_FREQUENCY the user is drunk
        for(Integer i:map.values()) {
            if(MAX_FREQUENCY < i)
                return true;
        }
        return false;
    }

    /*
     * Check if the word  typed by the user is not a correct word
     */
    private boolean isNotAWord(String text) {
        if(text.endsWith(" "))  {//Detect the end of a word
            //Get the last Word
            String[] split = text.split(" ");
            String lastWord = split[split.length-1];
            //Check if he is in the dictionary
            boolean isAWord= ! dictionary.isValidWord(lastWord);

            if(!isAWord)
                accOfwordWhishIsNotword++;

            //If there is too much word not in the dictionary the user is drunk
            if( accOfwordWhishIsNotword == MAX_NOT_A_WORD_PERMIT)   {
                //Reinitialisation
                accOfwordWhishIsNotword = 0;
                return true;
            }
        }
        return false;
    }

    /*
     * Check if the user type too long word
     */
    private boolean isTooLong(String text) {
        LOGGER.log(Level.INFO,"In isTooLong");

        //Calculate if the last word typing by the user has a higer size than SIZE_MAX
        String[] split = text.split(" ");
        for(String s: split)    {
            if( SIZE_MAX < s.length() )
                return true;
        }
        return false;
    }

    /*
     * Method need to be called when the user is drunk on his typing
     */
    @Override
    public void onDrunkKey(int primaryCode, int[] keyCodes) {

        this.buffer += "" + ((char)primaryCode);
        LOGGER.log(Level.INFO,"buffer : "+buffer);
        if(!WORD_KEY.startsWith(this.buffer)) {
            this.buffer = "";

            Toast.makeText(latinIME.getApplicationContext(), "Try again !", Toast.LENGTH_SHORT).show();
            return;
        }

        drunk = this.buffer.length() != WORD_KEY.length();
        LOGGER.log(Level.INFO,drunk + " " + (this.buffer.length() != WORD_KEY.length()) );


    }

    /*
     * Algorithm needed to be executed if the user is drunk
     */
    @Override
    public final void execute(InputConnection currentInputConnection) {

        //Remove all the text typed by the user
        removeText(currentInputConnection);
        //Set the state of the user as drunk
        setStateDrunk();
        //Make appear a toast to warn the user
        Toast.makeText(latinIME.getApplicationContext(),"You are drunk type \""+WORD_KEY+"\" to enables keyboard",Toast.LENGTH_SHORT).show();

    }

    private void setStateDrunk() {
        drunk = true;
    }

    /*
     * Remove the text of the input
     */
    private void removeText(InputConnection currentInputConnection) {
        int length = currentInputConnection.getTextBeforeCursor(1000, InputConnection.GET_TEXT_WITH_STYLES).length();
        //Simulating 1000 KEYCODE_DEL from the user
        for(int i=0; i < length;i++)    {
            latinIME.sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
    }



}
