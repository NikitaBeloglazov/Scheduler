package prototype.xd.scheduler.entities.settings_entries;

import static prototype.xd.scheduler.entities.settings_entries.SettingsEntryType.IMPORT_EXPORT_SETTINGS;

import android.content.ClipDescription;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

import prototype.xd.scheduler.databinding.ImportExportSettingsEntryBinding;
import prototype.xd.scheduler.utilities.Utilities;
import prototype.xd.scheduler.utilities.misc.ContextWrapper;
import prototype.xd.scheduler.utilities.misc.SettingsExporter;
import prototype.xd.scheduler.utilities.services.BackgroundSetterService;

public class ImportExportSettingsEntryConfig extends SettingsEntryConfig {
    
    private final ActivityResultLauncher<String[]> importLauncher;
    
    private final String[] mimetypes = {
            "file/*"
    };
    
    public ImportExportSettingsEntryConfig(@NonNull final ActivityResultLauncher<String[]> importLauncher) {
        this.importLauncher = importLauncher;
    }
    
    private void launchPicker() {
        importLauncher.launch(mimetypes);
    }
    
    @Override
    public int getRecyclerViewType() {
        return IMPORT_EXPORT_SETTINGS.ordinal();
    }
    
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void notifyFileChosen(@NonNull ContextWrapper wrapper, @Nullable Uri uri) {
        wrapper.processUri(uri, stream -> {
            if (SettingsExporter.tryImportSettings(wrapper.context, stream)) {
                BackgroundSetterService.exit(wrapper.context);
                wrapper.activity.finishAndRemoveTask();
            }
        });
    }
    
    static class ViewHolder extends SettingsEntryConfig.SettingsViewHolder<ImportExportSettingsEntryBinding, ImportExportSettingsEntryConfig> {
        
        
        ViewHolder(@NonNull final ContextWrapper wrapper,
                   @NonNull final ImportExportSettingsEntryBinding viewBinding) {
            super(wrapper, viewBinding);
        }
        
        @Override
        void bind(ImportExportSettingsEntryConfig config) {
            viewBinding.exportSettingsButton.setOnClickListener(v -> {
                File export = SettingsExporter.tryExportSettings(wrapper.context);
                if (export != null) {
                    Utilities.shareFiles(wrapper.context, ClipDescription.MIMETYPE_TEXT_PLAIN, List.of(export));
                }
            });
            viewBinding.importSettingsButton.setOnClickListener(v -> config.launchPicker());
        }
    }
}
