package prototype.xd.scheduler.utilities;

import static java.lang.Math.max;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BitmapUtilities {
    
    
    public static Bitmap fingerPrintAndSaveBitmap(Bitmap bitmap, File output, DisplayMetrics displayMetrics) throws IOException {
        Bitmap cut_bitmap = createScaledBitmap(bitmap, displayMetrics.widthPixels, displayMetrics.heightPixels, ScalingLogic.CROP);
        fingerPrintBitmap(cut_bitmap);
        
        FileOutputStream outputStream = new FileOutputStream(output);
        cut_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.close();
        
        Bitmap resizedBitmap = createScaledBitmap(cut_bitmap, (int) (cut_bitmap.getWidth() / 4f), (int) (cut_bitmap.getHeight() / 4f), BitmapUtilities.ScalingLogic.FIT);
        
        FileOutputStream outputStream_min = new FileOutputStream(output.getAbsolutePath() + "_min.png");
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream_min);
        outputStream_min.close();
        
        resizedBitmap.recycle();
        bitmap.recycle();
        return cut_bitmap;
    }
    
    public static Bitmap readStream(FileInputStream inputStream) throws IOException {
        Bitmap orig = BitmapFactory.decodeStream(inputStream);
        Bitmap copy = orig.copy(Bitmap.Config.ARGB_8888, true);
        orig.recycle();
        inputStream.close();
        return copy;
    }
    
    /**
     * Utility function for creating a scaled version of an existing bitmap
     *
     * @param unscaledBitmap Bitmap to scale
     * @param dstWidth       Wanted width of destination bitmap
     * @param dstHeight      Wanted height of destination bitmap
     * @param scalingLogic   Logic to use to avoid image stretching
     * @return New scaled bitmap object
     */
    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight, ScalingLogic scalingLogic) {
        
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        
        return scaledBitmap;
    }
    
    /**
     * ScalingLogic defines how scaling should be carried out if source and
     * destination image has different aspect ratio.
     * <p>
     * CROP: Scales the image the minimum amount while making sure that at least
     * one of the two dimensions fit inside the requested destination area.
     * Parts of the source image will be cropped to realize this.
     * <p>
     * FIT: Scales the image the minimum amount while making sure both
     * dimensions fit inside the requested destination area. The resulting
     * destination dimensions might be adjusted to a smaller size than
     * requested.
     */
    public enum ScalingLogic {
        CROP, FIT
    }
    
    /**
     * Calculate optimal down-sampling factor given the dimensions of a source
     * image, the dimensions of a destination area and a scaling logic.
     *
     * @param srcWidth     Width of source image
     * @param srcHeight    Height of source image
     * @param dstWidth     Width of destination area
     * @param dstHeight    Height of destination area
     * @param scalingLogic Logic to use to avoid image stretching
     * @return Optimal down scaling sample size for decoding
     */
    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                          ScalingLogic scalingLogic) {
        final float srcAspect = (float) srcWidth / (float) srcHeight;
        final float dstAspect = (float) dstWidth / (float) dstHeight;
        if (scalingLogic == ScalingLogic.FIT) {
            if (srcAspect > dstAspect) {
                return srcWidth / dstWidth;
            } else {
                return srcHeight / dstHeight;
            }
        } else {
            if (srcAspect > dstAspect) {
                return srcHeight / dstHeight;
            } else {
                return srcWidth / dstWidth;
            }
        }
    }
    
    /**
     * Calculates source rectangle for scaling bitmap
     *
     * @param srcWidth     Width of source image
     * @param srcHeight    Height of source image
     * @param dstWidth     Width of destination area
     * @param dstHeight    Height of destination area
     * @param scalingLogic Logic to use to avoid image stretching
     * @return Optimal source rectangle
     */
    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;
            
            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int) (srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            } else {
                final int srcRectHeight = (int) (srcWidth / dstAspect);
                final int scrRectTop = (srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }
    
    /**
     * Calculates destination rectangle for scaling bitmap
     *
     * @param srcWidth     Width of source image
     * @param srcHeight    Height of source image
     * @param dstWidth     Width of destination area
     * @param dstHeight    Height of destination area
     * @param scalingLogic Logic to use to avoid image stretching
     * @return Optimal destination rectangle
     */
    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;
            
            if (srcAspect > dstAspect) {
                return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }
    
    public static Paint createNewPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setColor(color);
        return paint;
    }
    
    public static int mixTwoColors(int color1, int color2, double balance) {
        Color c1 = Color.valueOf(color1);
        Color c2 = Color.valueOf(color2);
        float a = (float) (c1.alpha() * (1 - balance) + c2.alpha() * balance);
        float r = (float) (c1.red() * (1 - balance) + c2.red() * balance);
        float g = (float) (c1.green() * (1 - balance) + c2.green() * balance);
        float b = (float) (c1.blue() * (1 - balance) + c2.blue() * balance);
        return Color.argb(a, r, g, b);
    }
    
    public static int getAverageColor(Bitmap bitmap) {
        if (bitmap == null) return Color.BLACK;
        
        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;
        
        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        
        int actualPixelCount = 0;
        
        for (int y = 0, h = bitmap.getHeight(); y < h; y++) {
            for (int x = 0, w = bitmap.getWidth(); x < w; x++) {
                int color = pixels[x + y * w]; // x + y * width
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;
                if (red + green + blue != 0) {
                    redBucket += red;
                    greenBucket += green;
                    blueBucket += blue;
                    actualPixelCount++;
                }
            }
        }
        
        if(actualPixelCount == 0){
            return Color.BLACK;
        }
        
        pixelCount = actualPixelCount;
        
        if (max(max(redBucket, greenBucket), blueBucket) / pixelCount < 200) {
            redBucket += 50 * pixelCount;
            greenBucket += 50 * pixelCount;
            blueBucket += 50 * pixelCount;
        }
        
        return Color.argb(
                255,
                redBucket / pixelCount,
                greenBucket / pixelCount,
                blueBucket / pixelCount);
    }
    
    public static void fingerPrintBitmap(Bitmap bitmap) {
        bitmap.setPixel(0, 0, Color.DKGRAY);
        bitmap.setPixel(1, 0, Color.GREEN);
        bitmap.setPixel(0, 1, Color.YELLOW);
    }
    
    public static boolean noFingerPrint(Bitmap bitmap) {
        int pixel1 = bitmap.getPixel(0, 0);
        int pixel2 = bitmap.getPixel(1, 0);
        int pixel3 = bitmap.getPixel(0, 1);
        
        return !((pixel1 == Color.DKGRAY)
                && (pixel2 == Color.GREEN)
                && (pixel3 == Color.YELLOW));
    }
    
    public static int hashBitmap(Bitmap bitmap) {
        int[] buffer = new int[bitmap.getWidth() * bitmap.getHeight() / 16];
        bitmap.getPixels(buffer, 0, bitmap.getWidth() / 4, 0, 0, bitmap.getWidth() / 4, bitmap.getHeight() / 4);
        return Arrays.hashCode(buffer);
    }
    
}
