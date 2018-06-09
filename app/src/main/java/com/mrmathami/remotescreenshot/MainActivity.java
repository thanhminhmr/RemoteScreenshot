package com.mrmathami.remotescreenshot;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

public class MainActivity extends Activity {
    private static final int REQUEST_SCREENSHOT = 59706;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SCREENSHOT && resultCode == RESULT_OK) {
            startService(new Intent(this, RemoteScreenshotService.class)
                    .putExtra(RemoteScreenshotService.PARAMETER_SCREEN_CAPTURE_INTENT, data));
        }

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        if (mediaProjectionManager != null) {
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_SCREENSHOT
            );
        }
    }
}
