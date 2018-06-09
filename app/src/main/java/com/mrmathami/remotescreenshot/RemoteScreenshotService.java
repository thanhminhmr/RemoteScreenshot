package com.mrmathami.remotescreenshot;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.WindowManager;

import com.httpserver.HttpExchange;
import com.httpserver.HttpHandler;
import com.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Objects;

public class RemoteScreenshotService extends Service {
    // Notification
    static final int NOTIFY_ID = 9906;
    static final String CHANNEL_ID = BuildConfig.APPLICATION_ID;

    // Action List
    static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".STOP";

    // Parameter List
    static final String PARAMETER_SCREEN_CAPTURE_INTENT =
            BuildConfig.APPLICATION_ID + ".INTENT";

    // HTTP Server
    static final int SERVER_PORT = 56789;
    private HttpServer httpServer;
    private ScreenshotCreator screenshotCreator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (action == null) {
            if (httpServer == null) {
                final Intent screenCaptureIntent =
                        intent.getParcelableExtra(PARAMETER_SCREEN_CAPTURE_INTENT);

                doActionStart(screenCaptureIntent);
            }
        } else if (ACTION_STOP.equals(action)) {
            doActionStop();

            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }

    private void doActionStop() {
        httpServer.stop(0);
        screenshotCreator.stop();
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void doActionStart(Intent screenCaptureIntent) {
        if (screenCaptureIntent == null)
            return;

        setupVirtualDisplay(screenCaptureIntent);
        setupHttpServer();
        setupNotification();
    }

    private void setupHttpServer() {
        try {
            this.httpServer =
                    HttpServer.create(new InetSocketAddress(SERVER_PORT), 0);

            this.httpServer.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange httpExchange) {
                    try {
                        if (!httpExchange.getRequestMethod().equals("GET")
                                || !httpExchange.getRequestURI().getPath().equals("/")
                                && !httpExchange.getRequestURI().getPath().equals("/r")) {
                            // unsupported method
                            httpExchange.sendResponseHeaders(500, -1);
                            return;
                        }

                        // send back a bitmap image
                        httpExchange.getResponseHeaders().set("Content-Type", "image/bmp");

                        // check gzip supported
                        /*
                        boolean isGzipSupported = false;
                        final List<String> acceptEncodingList =
                                httpExchange.getRequestHeaders().get("Accept-Encoding");
                        if (acceptEncodingList != null) {
                            for (String acceptEncodingString : acceptEncodingList) {
                                if (acceptEncodingString.contains("gzip")) {
                                    isGzipSupported = true;
                                    break;
                                }
                            }
                        }

                        if (isGzipSupported) {
                            // gzip supported
                            httpExchange.getResponseHeaders().set("Content-Encoding", "gzip");
                        }
                        */
                        // get response output stream
                        OutputStream outputStream = httpExchange.getResponseBody();

                        // chunked encoding
                        httpExchange.sendResponseHeaders(200, 0);
                        /*
                        if (isGzipSupported) {
                            // create gzip output stream
                            outputStream = new GZIPOutputStream(outputStream, 65536);
                        }
                        */

                        // Screen-capture part
                        if (httpExchange.getRequestURI().getPath().equals("/")) {
                            screenshotCreator.getImage(outputStream);
                        } else {
                            Process process =
                                    Runtime.getRuntime()
                                            .exec("su -c screencap", null, null);
                            InputStream inputStream = process.getInputStream();
                            /*
                            byte[] buffer = new byte[65536];
                            int length = inputStream.read(buffer);
                            while (length != -1) {
                                outputStream.write(buffer, 0, length);
                                length = inputStream.read(buffer);
                            }
                            /*/
                            BitmapFileConverter.fromInputStream(inputStream, outputStream);
                            //*/
                            inputStream.close();
                        }

                        // done
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupVirtualDisplay(Intent screenCaptureIntent) {
        this.screenshotCreator = new ScreenshotCreator(
                Objects.requireNonNull((WindowManager) getSystemService(WINDOW_SERVICE))
                        .getDefaultDisplay(),
                Objects.requireNonNull((MediaProjectionManager)
                        getSystemService(MEDIA_PROJECTION_SERVICE))
                        .getMediaProjection(Activity.RESULT_OK, screenCaptureIntent)
        );
    }

    private void setupNotification() {
        // Notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            Objects.requireNonNull((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(notificationChannel);
        }

        final String address = getAddress();

        final NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(address)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(address))

                        .setSmallIcon(R.mipmap.ic_launcher)

                        .setAutoCancel(false)
                        .setDefaults(Notification.DEFAULT_ALL)

                        .addAction(R.drawable.ic_stop_black_24dp,
                                "Stop", buildPendingIntent(ACTION_STOP));

        startForeground(NOTIFY_ID, notificationBuilder.build());
    }

    private String getAddress() {
        StringBuilder address = new StringBuilder();
        try {
            // enum all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // enum all ip address in all network interfaces
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        address.append("http://")
                                .append(inetAddress.getHostAddress())
                                .append(':')
                                .append(Integer.toString(SERVER_PORT))
                                .append('\n');
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return address.toString();
    }

    private PendingIntent buildPendingIntent(String action) {
        return PendingIntent.getService(this, 0,
                new Intent(this, getClass()).setAction(action), 0);
    }
}
