package prototype.xd.scheduler;

import static prototype.xd.scheduler.utilities.Keys.GITHUB_ISSUES;
import static prototype.xd.scheduler.utilities.Utilities.displayToast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.SlidePolicy;

import prototype.xd.scheduler.utilities.Utilities;
import prototype.xd.scheduler.views.CheckBox;

public class IntroStartingFragment extends Fragment implements SlidePolicy {
    
    private CheckBox understoodCheckbox;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intro_starting_fragment, container, false);
        view.findViewById(R.id.github_button).setOnClickListener(v -> Utilities.openUrl(this, GITHUB_ISSUES));
        understoodCheckbox = view.findViewById(R.id.understood_checkbox);
        return view;
    }
    
    @Override
    public boolean isPolicyRespected() {
        return understoodCheckbox.isChecked();
    }
    
    @Override
    public void onUserIllegallyRequestedNextPage() {
        displayToast(requireContext(), R.string.please_read_the_notes);
    }
}
