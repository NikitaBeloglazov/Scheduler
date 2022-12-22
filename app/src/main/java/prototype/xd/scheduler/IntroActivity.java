package prototype.xd.scheduler;

import static prototype.xd.scheduler.utilities.PreferencesStore.preferences;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroCustomLayoutFragment;
import com.github.appintro.AppIntroPageTransformerType;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.HarmonizedColors;
import com.google.android.material.color.HarmonizedColorsOptions;
import com.google.android.material.color.MaterialColors;

import java.util.Locale;

import prototype.xd.scheduler.utilities.Keys;

public class IntroActivity extends AppIntro {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        HarmonizedColors.applyToContextIfAvailable(this,
                new HarmonizedColorsOptions.Builder()
                        .setColorResourceIds(new int[]{
                                R.color.gray_harmonized,
                                R.color.green_harmonized,
                                R.color.yellow_harmonized,
                                R.color.green_outline_harmonized,
                                R.color.yellow_outline_harmonized
                        })
                        .build());
        DynamicColors.applyToActivityIfAvailable(this);
        
        setTransformer(AppIntroPageTransformerType.Depth.INSTANCE);
        addSlide(new IntroStartingFragment());
        addSlide(new PermissionRequestFragment());
        if (isXiaomiPhone()) {
            addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_xiaomi_fragment));
        }
    
        View rootView = findViewById(android.R.id.content).getRootView();
        int surfaceColor = MaterialColors.getColor(rootView, R.attr.colorSurface);
        int primaryColor = MaterialColors.getColor(rootView, R.attr.colorPrimary);
        
        setNavBarColor(surfaceColor);
        showStatusBar(true);
        setStatusBarColor(surfaceColor);
        
        setSystemBackButtonLocked(true);
        setSkipButtonEnabled(false);
        
        setNextArrowColor(primaryColor);
        setSeparatorColor(primaryColor);
        setBarColor(surfaceColor);
        
        setDoneTextAppearance(R.style.MediumHeading);
        setColorDoneText(primaryColor);
        setDoneText(R.string.finish);
        setIndicatorColor(primaryColor, MaterialColors.getColor(rootView, R.attr.colorSurfaceVariant));
    }
    
    private boolean isXiaomiPhone() {
        return Build.MANUFACTURER.toLowerCase(Locale.ROOT).contains("xiaomi") || Build.MODEL.toLowerCase(Locale.ROOT).contains("xiaomi");
    }
    
    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // go back to the main activity
        IntroActivity.this.startActivity(new Intent(IntroActivity.this, MainActivity.class));
        preferences.edit().putBoolean(Keys.INTRO_SHOWN, true).apply();
        finish();
    }
}