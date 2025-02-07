package prototype.xd.scheduler.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import prototype.xd.scheduler.databinding.SettingsFragmentBinding;

// base dialog class with a list view
public class BaseListSettingsFragment<T extends RecyclerView.Adapter<?>> extends BaseSettingsFragment<SettingsFragmentBinding> {
    
    protected T listViewAdapter;
    
    @NonNull
    @Override
    public SettingsFragmentBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return SettingsFragmentBinding.inflate(inflater, container, false);
    }
    
    // view creation end (fragment visible)
    @Override
    @CallSuper
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(listViewAdapter);
    }
    
}
