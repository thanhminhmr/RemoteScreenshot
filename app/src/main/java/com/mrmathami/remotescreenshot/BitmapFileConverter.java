package com.mrmathami.remotescreenshot;

import android.graphics.PixelFormat;
import android.media.Image;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class BitmapFileConverter {
    private BitmapFileConverter() {
    }

    public static void fromImage(Image image, OutputStream outputStream) throws IOException {
        if (image.getFormat() == PixelFormat.RGBA_8888) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();

            // write bitmap header
            new BitmapFileHeader(image.getWidth(), image.getHeight()).write(outputStream);

            // write bitmap data
            byte[] row = new byte[planes[0].getRowStride()];
            for (int i = 0; i < image.getHeight(); i++) {
                buffer.get(row);
                outputStream.write(row, 0, image.getWidth() * 4);
            }
        } else {
            throw new UnsupportedOperationException(
                    "pixelFormat = " + Integer.toString(image.getFormat()) + " is not supported."
            );
        }
    }

    public static void fromInputStream(InputStream inputStream, OutputStream outputStream)
            throws IOException {

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int width = byteSwap(dataInputStream.readInt());
        int height = byteSwap(dataInputStream.readInt());
        int format = byteSwap(dataInputStream.readInt());

        if (format == PixelFormat.RGBA_8888) {
            // write bitmap header
            new BitmapFileHeader(width, height).write(outputStream);

            // write bitmap data
            byte[] row = new byte[width * 4];
            for (int i = 0; i < height; i++) {
                dataInputStream.readFully(row);
                outputStream.write(row);
            }
        } else {
            throw new UnsupportedOperationException(
                    "pixelFormat = " + Integer.toString(format) + " is not supported."
            );
        }
    }

    private static int byteSwap(int value) {
        return ((value << 24) & 0xFF000000)
                | ((value << 8) & 0x00FF0000)
                | ((value >> 8) & 0x0000FF00)
                | ((value >> 24) & 0x000000FF);
    }

    /**
     * Class BitmapFileHeader
     * BMP header specific for RGBX
     */
    private static class BitmapFileHeader {
        private byte[] data = new byte[]{
                (byte) 0x42, (byte) 0x4D, // short bfType = 'BM';
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int bfSize; ##########
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int bfReserved = 0;
                (byte) 0x42, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int bfOffBits = 66;

                (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biSize = 40;
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biWidth; ##########
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biHeight; ##########
                (byte) 0x01, (byte) 0x00, // short biPlanes = 1;
                (byte) 0x20, (byte) 0x00, // short biBitCount = 32;
                (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biCompression = BI_BITFIELDS;
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biSizeImage; ##########
                (byte) 0x13, (byte) 0x0B, (byte) 0x00, (byte) 0x00, // int biXPelsPerMeter = 2835;
                (byte) 0x13, (byte) 0x0B, (byte) 0x00, (byte) 0x00, // int biYPelsPerMeter = 2835;
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biClrUsed = 0;
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // int biClrImportant = 0;

                (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, // RGBQUAD redMask;
                (byte) 0x00, (byte) 0xFF, (byte) 0x00, (byte) 0x00, // RGBQUAD greenMask;
                (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0x00, // RGBQUAD blueMask;
        };

        BitmapFileHeader(int width, int height) {
            setDWORD(data, 2, width * height * 4 + 66); // int bfSize;
            setDWORD(data, 18, width); // int biWidth;
            setDWORD(data, 22, -height); // int biHeight;
            setDWORD(data, 34, width * height * 4); // int biSizeImage;
        }

        private static void setDWORD(byte[] data, int offset, int value) {
            data[offset] = (byte) (value & 0xFF);
            data[offset + 1] = (byte) ((value >> 8) & 0xFF);
            data[offset + 2] = (byte) ((value >> 16) & 0xFF);
            data[offset + 3] = (byte) ((value >> 24) & 0xFF);
        }

        void write(OutputStream outputStream) throws IOException {
            outputStream.write(data);
        }
    }
}

