package prototype.xd.scheduler.fragments;

import static prototype.xd.scheduler.utilities.ColorUtilities.getExpiredUpcomingColor;
import static prototype.xd.scheduler.utilities.ColorUtilities.getHarmonizedFontColorWithBg;
import static prototype.xd.scheduler.utilities.ColorUtilities.getHarmonizedSecondaryFontColorWithBg;
import static prototype.xd.scheduler.utilities.ColorUtilities.getOnBgColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.List;

import prototype.xd.scheduler.R;
import prototype.xd.scheduler.adapters.SettingsListViewAdapter;
import prototype.xd.scheduler.databinding.DraggableListEntryBinding;
import prototype.xd.scheduler.databinding.SortingSettingsFragmentBinding;
import prototype.xd.scheduler.entities.TodoEntry;
import prototype.xd.scheduler.entities.settings_entries.SettingsEntryConfig;
import prototype.xd.scheduler.entities.settings_entries.SwitchSettingsEntryConfig;
import prototype.xd.scheduler.utilities.Static;

public class SortingSettingsFragment extends BaseSettingsFragment<SortingSettingsFragmentBinding> {
    
    @NonNull
    @Override
    public SortingSettingsFragmentBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return SortingSettingsFragmentBinding.inflate(inflater, container, false);
    }
    
    // view creation end (fragment visible)
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        
        EntryTypeAdapter entryTypeAdapter = new EntryTypeAdapter();
        binding.orderRecyclerView.setLayoutManager(new LinearLayoutManager(wrapper.context));
        binding.orderRecyclerView.setAdapter(entryTypeAdapter);
        entryTypeAdapter.attachDragToRecyclerView(binding.orderRecyclerView);
        
        List<SettingsEntryConfig> settingsEntries = List.of(
                new SwitchSettingsEntryConfig(
                        Static.TREAT_GLOBAL_ITEMS_AS_TODAYS, getString(R.string.treat_global_as_todays),
                        (buttonView, isChecked) -> entryTypeAdapter.setGlobalEventsVisible(!isChecked), true));
        
        binding.settingsRecyclerView.setLayoutManager(new LinearLayoutManager(wrapper.context));
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(wrapper.context, LinearLayout.VERTICAL);
        divider.setLastItemDecorated(false);
        binding.settingsRecyclerView.addItemDecoration(divider);
        binding.settingsRecyclerView.setAdapter(new SettingsListViewAdapter(wrapper, settingsEntries));
    }
    
    private static final class EntryTypeAdapter extends RecyclerView.Adapter<EntryTypeAdapter.CardViewHolder> {
        
        @NonNull
        private final ItemTouchHelper itemDragHelper;
        @NonNull
        private final List<TodoEntry.EntryType> sortOrder;
        
        private boolean globalEventsVisible;
        
        private EntryTypeAdapter() {
            itemDragHelper = new ItemTouchHelper(new DragHelperCallback(this));
            sortOrder = Static.TODO_ITEM_SORTING_ORDER.get();
            globalEventsVisible = sortOrder.contains(TodoEntry.EntryType.GLOBAL);
        }
        
        public void attachDragToRecyclerView(RecyclerView recyclerView) {
            itemDragHelper.attachToRecyclerView(recyclerView);
        }
        
        public void setGlobalEventsVisible(boolean globalEventsVisible) {
            if (globalEventsVisible && !this.globalEventsVisible) {
                sortOrder.add(TodoEntry.EntryType.GLOBAL);
                notifyItemInserted(sortOrder.size());
            } else if (!globalEventsVisible && this.globalEventsVisible) {
                int indexOfGlobal = sortOrder.indexOf(TodoEntry.EntryType.GLOBAL);
                sortOrder.remove(indexOfGlobal);
                notifyItemRemoved(indexOfGlobal);
            }
            if (this.globalEventsVisible != globalEventsVisible) {
                Static.TODO_ITEM_SORTING_ORDER.put(sortOrder);
            }
            this.globalEventsVisible = globalEventsVisible;
        }
        
        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CardViewHolder(DraggableListEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        
        @Override
        public void onBindViewHolder(@NonNull CardViewHolder viewHolder, int position) {
            viewHolder.bind(sortOrder.get(position), itemDragHelper);
        }
        
        @Override
        public int getItemCount() {
            return sortOrder.size();
        }
        
        void swapCards(int fromPosition, int toPosition) {
            TodoEntry.EntryType fromNumber = sortOrder.get(fromPosition);
            sortOrder.set(fromPosition, sortOrder.get(toPosition));
            sortOrder.set(toPosition, fromNumber);
            Static.TODO_ITEM_SORTING_ORDER.put(sortOrder);
            notifyItemMoved(fromPosition, toPosition);
        }
        
        private static final class CardViewHolder extends RecyclerView.ViewHolder {
            
            @NonNull
            private final DraggableListEntryBinding binding;
            @NonNull
            private final Context context;
            
            private CardViewHolder(@NonNull DraggableListEntryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                context = binding.getRoot().getContext();
            }
            
            @SuppressLint("ClickableViewAccessibility")
            private void bind(@NonNull TodoEntry.EntryType entryType, @NonNull final ItemTouchHelper dragHelper) {
                String titleText;
                String descriptionText;
                int bgColor = Static.BG_COLOR.CURRENT.get();
                int fontColor = Static.FONT_COLOR.CURRENT.get();
                int borderColor = Static.BORDER_COLOR.CURRENT.get();
                switch (entryType) {
                    case UPCOMING:
                        titleText = context.getString(R.string.upcoming_events);
                        descriptionText = context.getString(R.string.upcoming_events_description);
                        bgColor = getExpiredUpcomingColor(bgColor, Static.BG_COLOR.UPCOMING.get());
                        fontColor = getExpiredUpcomingColor(fontColor, Static.FONT_COLOR.UPCOMING.get());
                        borderColor = getExpiredUpcomingColor(borderColor, Static.BORDER_COLOR.UPCOMING.get());
                        break;
                    case EXPIRED:
                        titleText = context.getString(R.string.expired_events);
                        descriptionText = context.getString(R.string.expired_events_description);
                        bgColor = getExpiredUpcomingColor(bgColor, Static.BG_COLOR.EXPIRED.get());
                        fontColor = getExpiredUpcomingColor(fontColor, Static.FONT_COLOR.EXPIRED.get());
                        borderColor = getExpiredUpcomingColor(borderColor, Static.BORDER_COLOR.EXPIRED.get());
                        break;
                    case TODAY:
                        titleText = context.getString(R.string.todays_events);
                        descriptionText = context.getString(R.string.todays_events_description);
                        break;
                    case GLOBAL:
                        titleText = context.getString(R.string.global_events);
                        descriptionText = context.getString(R.string.global_events_description);
                        break;
                    case UNKNOWN:
                    default:
                        titleText = "ERR/UNKNOWN";
                        descriptionText = "ERR/UNKNOWN";
                }
                
                binding.itemText.setText(titleText);
                binding.itemText.setTextColor(getHarmonizedFontColorWithBg(fontColor, bgColor));
                
                binding.itemDescriptionText.setText(descriptionText);
                binding.itemDescriptionText.setTextColor(getHarmonizedSecondaryFontColorWithBg(fontColor, bgColor));
                
                binding.card.setCardBackgroundColor(bgColor);
                binding.card.setStrokeColor(borderColor);
                
                binding.dragHandle.setImageTintList(ColorStateList.valueOf(getOnBgColor(bgColor)));
                binding.dragHandle.setOnTouchListener(
                        (v, event) -> {
                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                dragHelper.startDrag(this);
                                return true;
                            }
                            return false;
                        });
            }
        }
    }
    
    private static final class DragHelperCallback extends ItemTouchHelper.Callback {
        
        private final EntryTypeAdapter entryTypeAdapter;
        
        @Nullable
        private MaterialCardView dragCardView;
        
        private DragHelperCallback(EntryTypeAdapter entryTypeAdapter) {
            this.entryTypeAdapter = entryTypeAdapter;
        }
        
        @Override
        public int getMovementFlags(
                @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // draggable up and down, not swipe-able
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }
        
        @Override
        public boolean onMove(
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder from,
                @NonNull RecyclerView.ViewHolder to) {
            entryTypeAdapter.swapCards(
                    from.getBindingAdapterPosition(),
                    to.getBindingAdapterPosition());
            return true;
        }
        
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // swipe disabled
        }
        
        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                dragCardView = (MaterialCardView) viewHolder.itemView;
                dragCardView.setDragged(true);
            } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE && dragCardView != null) {
                dragCardView.setDragged(false);
                dragCardView = null;
            }
        }
    }
}
