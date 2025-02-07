package prototype.xd.scheduler.fragments;

import static prototype.xd.scheduler.utilities.Utilities.findFragmentInNavHost;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import java.util.Map;

import prototype.xd.scheduler.R;
import prototype.xd.scheduler.utilities.misc.ContextWrapper;
import prototype.xd.scheduler.utilities.Static;

// base dialog class that refreshes main screen on settings changes
public abstract class BaseSettingsFragment<T extends ViewBinding> extends DialogFragment {
    
    protected T binding;
    
    @SuppressLint("UnknownNullness")
    protected ContextWrapper wrapper;
    
    private Map<String, ?> preferenceStateBefore;
    
    public abstract T inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);
    
    // view creation begin
    @Override
    @MainThread
    @Nullable
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = inflate(inflater, container);
        preferenceStateBefore = Static.getAll();
        return binding.getRoot();
    }
    
    // fragment creation begin
    @Override
    @MainThread
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wrapper = ContextWrapper.from(this);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }
    
    // dialog dismissed (user pressed back button)
    @Override
    @CallSuper
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (!preferenceStateBefore.equals(Static.getAll())) {
            findFragmentInNavHost(requireActivity(), HomeFragment.class).notifyDatesetChanged();
        }
        super.onDismiss(dialog);
    }
}
