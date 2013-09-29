package fr.esiea.ail.drunkeyboard.implementation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
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

    public boolean takeApicture()  {
        final boolean intentAvailable = isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE);
        if(! intentAvailable )  {
            LOGGER.log(Level.SEVERE, "Impossible to open the camera capture intent");
            return false;
        }
        try {
            dispatchTakePictureIntent(1);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }

        return true;

    }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		takeApicture();
	}
    
    private void dispatchTakePictureIntent(int actionCode) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = createImageFile();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        startActivityForResult(takePictureIntent, actionCode);
    }



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
        
        
        LOGGER.log(Level.SEVERE, Environment.getExternalStorageDirectory().toString());
        
        File image = File.createTempFile(
                imageFileName,
                JPEG_FILE_SUFFIX,
                getAlbumDir()
        );
        return image;
    }

    public File getAlbumDir() {
        return  new File(Environment.getExternalStorageDirectory()+"/Pictures");
    }
}
