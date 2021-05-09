package prototype.xd.scheduler.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import prototype.xd.scheduler.FirstFragment;
import prototype.xd.scheduler.entities.TodoListEntry;

import static prototype.xd.scheduler.MainActivity.preferences;
import static prototype.xd.scheduler.utilities.BitmapUtilities.createSolidColorCircle;
import static prototype.xd.scheduler.utilities.Logger.ERROR;
import static prototype.xd.scheduler.utilities.Logger.INFO;
import static prototype.xd.scheduler.utilities.Logger.WARNING;
import static prototype.xd.scheduler.utilities.Logger.log;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored", "unchecked", "UseSwitchCompatOrMaterialCode"})
public class Utilities {
    
    public static File rootDir = new File(Environment.getExternalStorageDirectory().toString() + "/.Scheduler");
    
    public static void createRootIfNeeded() {
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }
    
    public static ArrayList<TodoListEntry> loadEntries() {
        try {
            
            ArrayList<String[]> entryParams = (ArrayList<String[]>) loadObject("list");
            ArrayList<String> entryGroupNames = (ArrayList<String>) loadObject("list_groupData");
            
            if (!(entryParams.size() == entryGroupNames.size())) {
                log(WARNING, "entryParams length: " + entryParams.size() + " entryGroupNames length: " + entryGroupNames.size());
            }
            
            ArrayList<TodoListEntry> readEntries = new ArrayList<>();
            for (int i = 0; i < entryParams.size(); i++) {
                readEntries.add(new TodoListEntry(entryParams.get(i), entryGroupNames.get(i)));
            }
            
            return sortEntries(readEntries);
        } catch (Exception e) {
            log(INFO, "no todo list");
            return new ArrayList<>();
        }
    }
    
    public static void saveEntries(ArrayList<TodoListEntry> entries) {
        try {
            ArrayList<String[]> entryParams = new ArrayList<>();
            ArrayList<String> entryGroupNames = new ArrayList<>();
            
            for (int i = 0; i < entries.size(); i++) {
                entryParams.add(entries.get(i).params);
                entryGroupNames.add(entries.get(i).group.name);
            }
            
            saveObject("list", entryParams);
            saveObject("list_groupData", entryGroupNames);
            log(INFO, "saving todo list");
        } catch (Exception e) {
            log(ERROR, "missing permission, failed to save todo list");
        }
    }
    
    public static Object loadObject(String fileName) throws IOException, ClassNotFoundException {
        File file = new File(rootDir, fileName);
        FileInputStream f = new FileInputStream(file);
        ObjectInputStream s = new ObjectInputStream(f);
        Object object = s.readObject();
        s.close();
        return object;
    }
    
    public static void saveObject(String fileName, Object object) throws IOException {
        File file = new File(rootDir, fileName);
        FileOutputStream f = new FileOutputStream(file);
        ObjectOutputStream s = new ObjectOutputStream(f);
        s.writeObject(object);
        s.close();
    }
    
    public static ArrayList<TodoListEntry> sortEntries(ArrayList<TodoListEntry> entries) {
        ArrayList<TodoListEntry> yesterdayEntries = new ArrayList<>();
        ArrayList<TodoListEntry> todayEntries = new ArrayList<>();
        ArrayList<TodoListEntry> globalEntries = new ArrayList<>();
        ArrayList<TodoListEntry> otherEntries = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).isTodayEntry) {
                todayEntries.add(entries.get(i));
            } else if (entries.get(i).isYesterdayEntry) {
                yesterdayEntries.add(entries.get(i));
            } else if (entries.get(i).isGlobalEntry) {
                globalEntries.add(entries.get(i));
            } else {
                otherEntries.add(entries.get(i));
            }
        }
        
        ArrayList<TodoListEntry> merged = new ArrayList<>();
        merged.addAll(todayEntries);
        merged.addAll(globalEntries);
        merged.addAll(yesterdayEntries);
        merged.addAll(otherEntries);
        Collections.sort(merged, new TodoListEntryGroupComparator());
        Collections.sort(merged, new TodoListEntryPriorityComparator());
        
        return merged;
    }
    
    public static String[] makeNewLines(String input, int maxChars) {
        return WordUtils.wrap(input, maxChars, "\n", true).split("\n");
    }
    
    public static void addSeekBarChangeListener(final TextView displayTo, SeekBar seekBar, final String key, int defValue, final int stringResource, final Fragment fragment) {
        displayTo.setText(fragment.getString(stringResource, preferences.getInt(key, defValue)));
        seekBar.setProgress(preferences.getInt(key, defValue));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                displayTo.setText(fragment.getString(stringResource, progress));
                preferences.edit().putInt(key, progress).apply();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferences.edit().putBoolean("settingsModified", true).apply();
            }
        });
    }
    
    public static void addSeekBarChangeListener(final TextView displayTo, SeekBar seekBar, int value, final int stringResource, final FirstFragment fragment, final TodoListEntry todoListEntry, final String parameter, final TextView stateIcon) {
        displayTo.setText(fragment.getString(stringResource, (int) value));
        seekBar.setProgress(value);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                displayTo.setText(fragment.getString(stringResource, progress));
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            
            }
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                todoListEntry.changeParameter(parameter, String.valueOf(seekBar.getProgress()));
                saveEntries(fragment.todoListEntries);
                preferences.edit().putBoolean("need_to_reconstruct_bitmap", true).apply();
                todoListEntry.setStateIconColor(stateIcon, parameter);
            }
        });
    }
    
    public static void addSwitchChangeListener(final Switch tSwitch, final String key, boolean defValue, final CompoundButton.OnCheckedChangeListener listener) {
        tSwitch.setChecked(preferences.getBoolean(key, defValue));
        tSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(key, isChecked).apply();
                preferences.edit().putBoolean("settingsModified", true).apply();
                if (!(listener == null)) {
                    listener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
    }
    
    public static void addSwitchChangeListener(final Switch tSwitch, boolean value, final TodoListEntry entry, final String parameter, final FirstFragment fragment, final TextView stateIcon) {
        tSwitch.setChecked(value);
        tSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                entry.changeParameter(parameter, String.valueOf(isChecked));
                saveEntries(fragment.todoListEntries);
                preferences.edit().putBoolean("need_to_reconstruct_bitmap", true).apply();
                entry.setStateIconColor(stateIcon, parameter);
            }
        });
    }
    
    public static void invokeColorDialogue(final String key, final ImageView target, final int defValue, Context context) {
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Выберите цвет")
                .initialColor(preferences.getInt(key, defValue))
                .showAlphaSlider(false)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Применить", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        preferences.edit().putInt(key, selectedColor).apply();
                        preferences.edit().putBoolean("settingsModified", true).apply();
                        target.setImageBitmap(createSolidColorCircle(preferences.getInt(key, defValue)));
                    }
                }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            
            }
        }).build().show();
    }
    
    public static void invokeColorDialogue(final int value, final ImageView target, Context context, final TodoListEntry todoListEntry, final String parameter, final boolean setReconstructFlag, final TextView stateIcon) {
        ColorPickerDialogBuilder
                .with(context)
                .setTitle("Выберите цвет")
                .initialColor(value)
                .showAlphaSlider(false)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Применить", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        todoListEntry.changeParameter(parameter, String.valueOf(selectedColor));
                        target.setImageBitmap(createSolidColorCircle(selectedColor));
                        preferences.edit().putBoolean("need_to_reconstruct_bitmap", setReconstructFlag).apply();
                        todoListEntry.setStateIconColor(stateIcon, parameter);
                    }
                }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            
            }
        }).build().show();
    }
}

class TodoListEntryPriorityComparator implements Comparator<TodoListEntry> {
    @Override
    public int compare(TodoListEntry o1, TodoListEntry o2) {
        return Integer.compare(o2.priority, o1.priority);
    }
}

class TodoListEntryGroupComparator implements Comparator<TodoListEntry> {
    @Override
    public int compare(TodoListEntry o1, TodoListEntry o2) {
        return Integer.compare(o1.group.name.hashCode(), o2.group.name.hashCode());
    }
}
