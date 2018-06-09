package com.mrmathami.remotescreenshot;

import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.IOException;
import java.io.OutputStream;

final class ScreenshotCreator extends MediaProjection.Callback {

    // Virtual display
    static final String VIRTUAL_DISPLAY_NAME = BuildConfig.APPLICATION_ID;

    private final Display defaultDisplay;
    private final MediaProjection mediaProjection;

    private final int displayWidth;
    private final int displayHeight;
    private final int displayDpi;
    private final int displayRotation;
    private final ImageReader imageReader;
    private final ImageReader imageReaderRotation;
    private final VirtualDisplay virtualDisplay;
    private int lastRotation;

    public ScreenshotCreator(Display defaultDisplay, MediaProjection mediaProjection) {
        // init objects
        this.defaultDisplay = defaultDisplay;
        this.mediaProjection = mediaProjection;

        // get display metrics
        final DisplayMetrics displayMetrics = getDisplayMetrics(defaultDisplay);
        displayWidth = displayMetrics.widthPixels;
        displayHeight = displayMetrics.heightPixels;
        displayDpi = displayMetrics.densityDpi;
        displayRotation = defaultDisplay.getRotation();

        // setup virtual display
        imageReader = ImageReader.newInstance(
                displayWidth, displayHeight, PixelFormat.RGBA_8888, 2);

        imageReaderRotation = ImageReader.newInstance(
                displayHeight, displayWidth, PixelFormat.RGBA_8888, 2);

        virtualDisplay =
                mediaProjection.createVirtualDisplay(VIRTUAL_DISPLAY_NAME,
                        displayMetrics.widthPixels, displayMetrics.heightPixels,
                        displayMetrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        imageReader.getSurface(), null, null);

        mediaProjection.registerCallback(this, null);
    }

    private static DisplayMetrics getDisplayMetrics(Display display) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        return displayMetrics;
    }

    public synchronized void getImage(OutputStream outputStream) {
        int currentRotation = defaultDisplay.getRotation();
        if (lastRotation != currentRotation) {
            if (displayRotation == currentRotation) {
                virtualDisplay.resize(displayWidth, displayHeight, displayDpi);
                virtualDisplay.setSurface(imageReader.getSurface());
            } else {
                virtualDisplay.resize(displayHeight, displayWidth, displayDpi);
                virtualDisplay.setSurface(imageReaderRotation.getSurface());
            }
            lastRotation = currentRotation;
        }
        Image image = null;
        while (image == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // intended
            }
            if (displayRotation == currentRotation) {
                image = imageReader.acquireLatestImage();
            } else {
                image = imageReaderRotation.acquireLatestImage();
            }
        }
        //virtualDisplay.setSurface(null);

        try {
            BitmapFileConverter.fromImage(image, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            image.close();
        }
    }

    public synchronized void stop() {
        mediaProjection.stop();
        imageReader.close();
        imageReaderRotation.close();
    }

    @Override
    public void onStop() {
        virtualDisplay.release();
    }
}
