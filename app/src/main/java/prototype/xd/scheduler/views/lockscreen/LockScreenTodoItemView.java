package prototype.xd.scheduler.views.lockscreen;

import static prototype.xd.scheduler.utilities.GraphicsUtilities.getAverageColor;
import static prototype.xd.scheduler.utilities.GraphicsUtilities.getHarmonizedFontColorWithBg;
import static prototype.xd.scheduler.utilities.GraphicsUtilities.getHarmonizedSecondaryFontColorWithBg;
import static prototype.xd.scheduler.utilities.DateManager.currentDayUTC;
import static prototype.xd.scheduler.utilities.Keys.DEFAULT_TITLE_FONT_SIZE_MULTIPLIER;
import static prototype.xd.scheduler.utilities.Keys.DISPLAY_METRICS_DENSITY;
import static prototype.xd.scheduler.utilities.Keys.ITEM_FULL_WIDTH_LOCK;
import static prototype.xd.scheduler.utilities.Keys.SHOW_GLOBAL_ITEMS_LABEL_LOCK;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.viewbinding.ViewBinding;

import java.util.regex.Pattern;

import prototype.xd.scheduler.R;
import prototype.xd.scheduler.databinding.BasicEntryBinding;
import prototype.xd.scheduler.databinding.RoundedEntryBinding;
import prototype.xd.scheduler.databinding.SleekEntryBinding;
import prototype.xd.scheduler.entities.TodoEntry;
import prototype.xd.scheduler.utilities.Keys;

// base class for lockscreen todolist entries
public abstract class LockScreenTodoItemView<V extends ViewBinding> {
    
    @NonNull
    protected final V viewBinding;
    @NonNull
    private final View root;
    private final Context context;
    
    private static final Pattern timeSplitPattern = Pattern.compile(" - ");
    
    LockScreenTodoItemView(@NonNull V binding) {
        viewBinding = binding;
        root = binding.getRoot();
        context = binding.getRoot().getContext();
    }
    
    protected abstract View getClickableRoot();
    
    @NonNull
    public LockScreenTodoItemView<V> setOnClickListener(@Nullable View.OnClickListener onClickListener) {
        View view = getClickableRoot();
        view.setFocusable(true);
        TypedValue themedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, themedValue, true);
        view.setForeground(AppCompatResources.getDrawable(context, themedValue.resourceId));
        view.setOnClickListener(onClickListener);
        return this;
    }
    
    @NonNull
    public LockScreenTodoItemView<V> addToContainer(@NonNull ViewGroup container) {
        container.addView(root);
        return this;
    }
    
    public abstract void setBackgroundColor(@ColorInt int color);
    
    public abstract void setBorderColor(@ColorInt int color);
    
    public abstract void setTitleTextColor(@ColorInt int color);
    
    public abstract void setIndicatorColor(@ColorInt int color);
    
    public abstract void setTimeTextColor(@ColorInt int color);
    
    // should not be overridden
    public void setBorderSizeDP(int sizeDP) {
        // convert to dp to pixels
        setBorderSizePX((int) (sizeDP * DISPLAY_METRICS_DENSITY.get()));
    }
    
    public abstract void setBorderSizePX(int sizePX);
    
    
    public abstract void setTitleTextSize(float sizeSP);
    
    public abstract void setTimeTextSize(float sizeSP);
    
    public void setCombinedTextSize(float sizeSP) {
        setTitleTextSize(sizeSP * DEFAULT_TITLE_FONT_SIZE_MULTIPLIER);
        setTimeTextSize(sizeSP);
    }
    
    public void setTimeStartText(@NonNull String text) {
        // empty by default, not all views support this
    }
    
    public abstract void setTitleText(@NonNull String text);
    
    public abstract void setTimeSpanText(@NonNull String text);
    
    
    public abstract void hideIndicatorAndTime();
    
    
    @NonNull
    public LockScreenTodoItemView<V> applyLayoutIndependentParameters(@NonNull TodoEntry entry) {
        
        int fontSizeSP = Keys.FONT_SIZE.get();
        
        setBorderSizeDP(entry.borderThickness.get(currentDayUTC));
        
        setTitleText(entry.getTextOnDay(currentDayUTC, context, SHOW_GLOBAL_ITEMS_LABEL_LOCK.get()));
        setTitleTextSize(fontSizeSP * DEFAULT_TITLE_FONT_SIZE_MULTIPLIER);
        
        if (entry.isFromSystemCalendar()) {
            String timeSpan = entry.getCalendarEntryTimeSpan(context, currentDayUTC);
            setTimeSpanText(timeSpan);
            
            setTimeStartText(timeSplitPattern.split(timeSpan)[0]);
            setTimeTextSize(fontSizeSP);
            setIndicatorColor(entry.event.color);
        } else {
            hideIndicatorAndTime();
        }
        
        viewBinding.getRoot().setLayoutParams(new LinearLayout.LayoutParams(
                ITEM_FULL_WIDTH_LOCK.get() ?
                        LinearLayout.LayoutParams.MATCH_PARENT : LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        return this;
    }
    
    public void applyLayoutDependentParameters(@NonNull TodoEntry entry, @NonNull Bitmap bgBitmap, @NonNull ViewGroup container) {
        
        if (entry.isAdaptiveColorEnabled()) {
            int width = root.getWidth();
            int height = root.getHeight();
            
            int[] pixels = new int[width * height];
            //                                                                                 add container y offset
            bgBitmap.getPixels(pixels, 0, width, (int) root.getX(), (int) (root.getY() + container.getY()), width, height);
            entry.setAverageBackgroundColor(getAverageColor(pixels));
        }
        
        mixAndSetBgAndTextColors(entry.isFromSystemCalendar(),
                entry.fontColor.get(currentDayUTC),
                entry.getAdaptiveColor(entry.bgColor.get(currentDayUTC)));
        setBorderColor(entry.getAdaptiveColor(entry.borderColor.get(currentDayUTC)));
    }
    
    public void mixAndSetBgAndTextColors(boolean setTimeTextColor, int fontColor, int backgroundColor) {
        // setup colors
        setBackgroundColor(backgroundColor);
        setTitleTextColor(getHarmonizedFontColorWithBg(fontColor, backgroundColor));
        if (setTimeTextColor) {
            setTimeTextColor(getHarmonizedSecondaryFontColorWithBg(fontColor, backgroundColor));
        }
    }
    
    public enum TodoItemViewType {
        BASIC, ROUNDED, SLEEK
    }
    
    @NonNull
    public static LockScreenTodoItemView<?> inflateViewByType(@NonNull TodoItemViewType todoItemViewType, @Nullable ViewGroup parent, @NonNull LayoutInflater layoutInflater) {
        switch (todoItemViewType) {
            case SLEEK:
                return new SleekLockScreenTodoItemView(SleekEntryBinding.inflate(layoutInflater, parent, false));
            case ROUNDED:
                return new RoundedLockScreenTodoItem(RoundedEntryBinding.inflate(layoutInflater, parent, false));
            case BASIC:
            default:
                return new BasicLockScreenTodoItemView(BasicEntryBinding.inflate(layoutInflater, parent, false));
        }
    }
}
