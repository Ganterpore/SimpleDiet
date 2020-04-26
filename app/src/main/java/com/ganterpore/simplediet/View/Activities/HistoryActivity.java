package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DailyMeals;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.Model.DietPlan;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.ganterpore.simplediet.View.Activities.MainActivity.SHARED_PREFS_LOC;

public class HistoryActivity extends Fragment {
    public static final String TAG = "HistoryActivity";
    DietController dietController;
    RecyclerView weeksHistory;
    private boolean trackWater;
    private boolean trackAlcohol;
    private boolean trackCaffeine;
    private boolean trackCheats;

    private View historyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        historyView = inflater.inflate(R.layout.activity_history, container, false);
        return historyView;
    }

    @Override
    public void onStart() {
        super.onStart();
        dietController = BasicDietController.getInstance();
        setMonthlyView();

        weeksHistory = historyView.findViewById(R.id.weeks_history);
        weeksHistory.setAdapter(new WeekHistoryAdapter(getActivity(), 8));

        SharedPreferences preferences = getActivity().getSharedPreferences(SHARED_PREFS_LOC, MODE_PRIVATE);
        trackWater = preferences.getBoolean("track_water", true);
        trackAlcohol = preferences.getBoolean("track_alcohol", true);
        trackCaffeine = preferences.getBoolean("track_caffeine", true);
        trackCheats = preferences.getBoolean("track_cheats", true);
        if (!trackWater) {
            historyView.findViewById(R.id.monthly_water_container).setVisibility(View.GONE);
        }
        if (!trackCheats) {
            historyView.findViewById(R.id.monthly_cheat_container).setVisibility(View.GONE);
        }
        if (!trackCaffeine) {
            historyView.findViewById(R.id.monthly_caffeine_container).setVisibility(View.GONE);
        }
        if (!trackAlcohol) {
            historyView.findViewById(R.id.monthly_alcohol_container).setVisibility(View.GONE);
        }
    }

    /**
     * Sets up all the values of the monthly view
     */
    private void setMonthlyView() {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        View monthlyContainer = historyView.findViewById(R.id.monthly_container);

        WeeklyIntake week1 = dietController.getWeeksIntake(0);
        WeeklyIntake week2 = dietController.getWeeksIntake(1);
        WeeklyIntake week3 = dietController.getWeeksIntake(2);
        WeeklyIntake week4 = dietController.getWeeksIntake(3);

        //get the text views from the main activity
        TextView monthlyVegTV = historyView.findViewById(R.id.monthly_veges_intake);
        TextView monthlyProteinTV = historyView.findViewById(R.id.monthly_protein_intake);
        TextView monthlyDairyTV = historyView.findViewById(R.id.monthly_dairy_intake);
        TextView monthlyGrainTV = historyView.findViewById(R.id.monthly_grain_intake);
        TextView monthlyFruitTV = historyView.findViewById(R.id.monthly_fruit_intake);
        TextView monthlyWaterTV = historyView.findViewById(R.id.monthly_water_intake);
        TextView monthlyCaffeineTV = historyView.findViewById(R.id.monthly_caffeine_count);
        TextView monthlyAlcoholTV = historyView.findViewById(R.id.monthly_alcohol_count);
        TextView monthlyCheatTV = historyView.findViewById(R.id.monthly_cheat_count);

        //get the progress bars from the main activity
        ProgressBar monthlyCheatsPB = monthlyContainer.findViewById(R.id.progress_cheats);

        //creating arrays of the text views to update
        TextView[] textViewsCount = {monthlyVegTV, monthlyProteinTV, monthlyDairyTV, monthlyGrainTV, monthlyFruitTV, monthlyWaterTV,
                monthlyCaffeineTV, monthlyAlcoholTV, monthlyCheatTV};
        double[] counts = {week1.getVegCount()+week2.getVegCount()+week3.getVegCount()+week4.getVegCount(),
                        week1.getProteinCount()+week2.getProteinCount()+week3.getProteinCount()+week4.getProteinCount(),
                        week1.getDairyCount()+week2.getDairyCount()+week3.getDairyCount()+week4.getDairyCount(),
                        week1.getGrainCount()+week2.getGrainCount()+week3.getGrainCount()+week4.getGrainCount(),
                        week1.getFruitCount()+week2.getFruitCount()+week3.getFruitCount()+week4.getFruitCount(),
                        week1.getWaterCount()+week2.getWaterCount()+week3.getWaterCount()+week4.getWaterCount(),
                        week1.getCaffieneCount()+week2.getCaffieneCount()+week3.getCaffieneCount()+week4.getCaffieneCount(),
                        week1.getAlcoholCount()+week2.getAlcoholCount()+week3.getAlcoholCount()+week4.getAlcoholCount(),
                        week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats()};
        double[] plans = {week1.getWeeklyLimitVeg()+week2.getWeeklyLimitVeg()+week3.getWeeklyLimitVeg()+week4.getWeeklyLimitVeg(),
                        week1.getWeeklyLimitProtein()+week2.getWeeklyLimitProtein()+week3.getWeeklyLimitProtein()+week4.getWeeklyLimitProtein(),
                        week1.getWeeklyLimitDairy()+week2.getWeeklyLimitDairy()+week3.getWeeklyLimitDairy()+week4.getWeeklyLimitDairy(),
                        week1.getWeeklyLimitGrain()+week2.getWeeklyLimitGrain()+week3.getWeeklyLimitGrain()+week4.getWeeklyLimitGrain(),
                        week1.getWeeklyLimitFruit()+week2.getWeeklyLimitFruit()+week3.getWeeklyLimitFruit()+week4.getWeeklyLimitFruit(),
                        week1.getWeeklyLimitHydration()+week2.getWeeklyLimitHydration()+week3.getWeeklyLimitHydration()+week4.getWeeklyLimitHydration(),
                        week1.getWeeklyLimitCaffiene()+week2.getWeeklyLimitCaffiene()+week3.getWeeklyLimitCaffiene()+week4.getWeeklyLimitCaffiene(),
                        week1.getWeeklyLimitAlcohol()+week2.getWeeklyLimitAlcohol()+week3.getWeeklyLimitAlcohol()+week4.getWeeklyLimitAlcohol(),
                        week1.getWeeklyLimitCheats()+week2.getWeeklyLimitCheats()+week3.getWeeklyLimitCheats()+week4.getWeeklyLimitCheats()};

        //updating text for all the main food groups
        for(int i=0;i<textViewsCount.length;i++) {
            //getting values from the arrays for this index
            TextView countTV = textViewsCount[i];
            double count = counts[i];
            double plan = plans[i];

            countTV.setText(String.format("%s/%s", df.format(count), df.format(plan)));
        }

        monthlyCheatsPB.setMax((int) (week1.getWeeklyLimitCheats() * 4 * SCALE_FACTOR));
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(monthlyCheatsPB, "progress",
                (int) (week1.getTotalCheats()+week2.getTotalCheats()+week3.getTotalCheats()+week4.getTotalCheats())*SCALE_FACTOR);
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    /**
     * Adapter for the weeks information
     */
    public class WeekHistoryAdapter extends RecyclerView.Adapter<WeekHistoryViewHolder> {
        int nWeeks;
        Activity activity;
        SparseBooleanArray nDaysAgoVisible;

        WeekHistoryAdapter(Activity activity, int nWeeks) {
            this.activity = activity;
            this.nWeeks = nWeeks;
            this.nDaysAgoVisible = new SparseBooleanArray();
        }

        @NonNull
        @Override
        public WeekHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_weekly_data, viewGroup, false);
            return new WeekHistoryViewHolder(activity, view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull WeekHistoryViewHolder weekHistoryViewHolder, int position) {
            weekHistoryViewHolder.build(position);
        }

        @Override
        public int getItemCount() {
            return nWeeks;
        }
    }


    /**
     * View Holder for the information about a week.
     */
    private class WeekHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("MMM dd");
        Activity activity;
        WeekHistoryAdapter adapter;
        int nWeeksAgo;

        WeekHistoryViewHolder(Activity activity, @NonNull View itemView, WeekHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.adapter = adapter;
        }

        public void build(final int nWeeksAgo) {
            this.nWeeksAgo = nWeeksAgo;
            //get the correct week from position
            WeeklyIntake week = dietController.getWeeksIntake(nWeeksAgo);

            //updating date information
            TextView dateTV = itemView.findViewById(R.id.date);
            dateTV.setText(String.format("%s - %s", dateFormat.format(week.getStartDate()), dateFormat.format(week.getEndDate().getTime() - DateUtils.DAY_IN_MILLIS)));
            final int SCALE_FACTOR = 100;

            //getting and updating values for the views
            CompletableItemView completedFoodView = itemView.findViewById(R.id.completed_food);
            CompletableItemView completedWaterView = itemView.findViewById(R.id.completed_water);
            CompletableItemView didntCheatView = itemView.findViewById(R.id.didnt_cheat);
            ProgressBar cheatsProgress = itemView.findViewById(R.id.progress_cheats);

            completedFoodView.setCompleted(week.isFoodCompleted());
            completedWaterView.setCompleted(week.isHydrationCompleted());
            didntCheatView.setCompleted(!week.isOverCheatScore());
            cheatsProgress.setMax((int) week.getWeeklyLimitCheats() * SCALE_FACTOR);
            cheatsProgress.setProgress((int) week.getTotalCheats() * SCALE_FACTOR);
            if(!trackCheats) {
                itemView.findViewById(R.id.cheat_progress_container).setVisibility(View.GONE);
            } if(!trackWater) {
                completedWaterView.setVisibility(View.GONE);
            }

            final View expandableView = itemView.findViewById(R.id.expanded_layout);
            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);

            //making sure view is shown as it should be, either expanded or not
            if (adapter.nDaysAgoVisible.get(nWeeksAgo, false)) {
                expandableView.setVisibility(View.VISIBLE);
                dropdownButton.setRotation(180);
            } else {
                expandableView.setVisibility(View.GONE);
                dropdownButton.setRotation(0);
            }

            //creating functionality for the button that shows expands the card
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (expandableView.getVisibility() == View.GONE) {
                        expandableView.setVisibility(View.VISIBLE);
                        dropdownButton.animate().setDuration(200).rotation(180);
                        adapter.nDaysAgoVisible.append(nWeeksAgo, true);
                        adapter.notifyItemChanged(nWeeksAgo);
                    } else {
                        expandableView.setVisibility(View.GONE);
                        dropdownButton.animate().setDuration(200).rotation(0);
                        adapter.nDaysAgoVisible.append(nWeeksAgo, false);
                        adapter.notifyItemChanged(nWeeksAgo);
                    }
                }
            });

            //updating text views
            NumberFormat df = new DecimalFormat("##.##");
            TextView vegCount = itemView.findViewById(R.id.weekly_veges_intake);
            TextView proteinCount = itemView.findViewById(R.id.weekly_protein_intake);
            TextView dairyCount = itemView.findViewById(R.id.weekly_dairy_intake);
            TextView grainCount = itemView.findViewById(R.id.weekly_grain_intake);
            TextView fruitCount = itemView.findViewById(R.id.weekly_fruit_intake);
            TextView waterCount = itemView.findViewById(R.id.weekly_water_intake);
            TextView cheatCount = itemView.findViewById(R.id.weekly_cheat_count);
            TextView caffeineCount = itemView.findViewById(R.id.weekly_caffeine_count);
            TextView alcoholCount = itemView.findViewById(R.id.weekly_alcohol_count);

            vegCount.setText((df.format(week.getVegCount()) + "/" + df.format(week.getWeeklyLimitVeg())));
            proteinCount.setText((df.format(week.getProteinCount()) + "/" + df.format(week.getWeeklyLimitProtein())));
            dairyCount.setText((df.format(week.getDairyCount()) + "/" + df.format(week.getWeeklyLimitDairy())));
            grainCount.setText((df.format(week.getGrainCount()) + "/" + df.format(week.getWeeklyLimitGrain())));
            fruitCount.setText((df.format(week.getFruitCount()) + "/" + df.format(week.getWeeklyLimitFruit())));
            waterCount.setText((df.format(week.getHydrationScore()) + "/" + df.format(week.getWeeklyLimitHydration())));
            cheatCount.setText((df.format(week.getTotalCheats()) + "/" + df.format(week.getWeeklyLimitCheats())));
            caffeineCount.setText((df.format(week.getCaffieneCount()) + "/" + df.format(week.getWeeklyLimitCaffiene())));
            alcoholCount.setText((df.format(week.getAlcoholCount()) + "/" + df.format(week.getWeeklyLimitAlcohol())));

            if(!trackCheats) {
                itemView.findViewById(R.id.weekly_cheat_container).setVisibility(View.GONE);
            } if(!trackWater) {
                itemView.findViewById(R.id.weekly_water_container).setVisibility(View.GONE);
            } if(!trackAlcohol) {
                itemView.findViewById(R.id.weekly_alcohol_container).setVisibility(View.GONE);
            } if(!trackCaffeine) {
                itemView.findViewById(R.id.weekly_caffeine_container).setVisibility(View.GONE);
            }

            RecyclerView daysHistory = itemView.findViewById(R.id.days_list);
            daysHistory.setAdapter(new DaysHistoryAdapter(activity, 7, nWeeksAgo));
        }
    }

    /**
     * Adapter for the weeks information
     */
    public class DaysHistoryAdapter extends RecyclerView.Adapter<DaysHistoryViewHolder> {
        int nDays;
        Activity activity;
        SparseBooleanArray nDaysAgoVisible;
        int weeksAgo;

        DaysHistoryAdapter(Activity activity, int nDays, int weeksAgo) {
            this.activity = activity;
            this.nDays = nDays;
            this.nDaysAgoVisible = new SparseBooleanArray();
            this.weeksAgo = weeksAgo;
        }

        @NonNull
        @Override
        public DaysHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            View view;
            view = LayoutInflater.from(activity).inflate(R.layout.list_item_day_summary, viewGroup, false);
            return new HistoryActivity.DaysHistoryViewHolder(activity, view, this);
        }

        @Override
        public void onBindViewHolder(@NonNull DaysHistoryViewHolder daysHistoryViewHolder, int position) {
            daysHistoryViewHolder.build(position, weeksAgo);
        }

        @Override
        public int getItemCount() {
            return nDays;
        }
    }


    /**
     * View Holder for the information about a week.
     */
    private class DaysHistoryViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Activity activity;
        DaysHistoryAdapter adapter;

        DaysHistoryViewHolder(Activity activity, @NonNull View itemView, DaysHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.adapter = adapter;
        }

        public void build(final int dayNo, int nWeeksAgo) {
            DailyMeals day = dietController.getDaysMeals(dayNo + 7*nWeeksAgo);
            DietPlan daysPlan = dietController.getDaysDietPlan(dayNo + 7*nWeeksAgo);

            //updating date information
            TextView dateTV = itemView.findViewById(R.id.date);
            dateTV.setText(dateFormat.format(day.getDate()));

            //updating text views
            NumberFormat df = new DecimalFormat("##.##");
            TextView vegCount = itemView.findViewById(R.id.veg_count);
            TextView proteinCount = itemView.findViewById(R.id.protein_count);
            TextView dairyCount = itemView.findViewById(R.id.dairy_count);
            TextView grainCount = itemView.findViewById(R.id.grain_count);
            TextView fruitCount = itemView.findViewById(R.id.fruit_count);
            TextView waterCount = itemView.findViewById(R.id.water_count);
            TextView cheatCount = itemView.findViewById(R.id.cheat_count);

            vegCount.setText((df.format(day.getVegCount()) + "/" + df.format(daysPlan.getDailyVeges())));
            proteinCount.setText((df.format(day.getProteinCount()) + "/" + df.format(daysPlan.getDailyProtein())));
            dairyCount.setText((df.format(day.getDairyCount()) + "/" + df.format(daysPlan.getDailyDairy())));
            grainCount.setText((df.format(day.getGrainCount()) + "/" + df.format(daysPlan.getDailyGrain())));
            fruitCount.setText((df.format(day.getFruitCount()) + "/" + df.format(daysPlan.getDailyFruit())));
            waterCount.setText((df.format(day.getHydrationScore()) + "/" + df.format(daysPlan.getDailyHydration())));
            cheatCount.setText((df.format(day.getTotalCheats()) + "/" + df.format(daysPlan.getDailyCheats())));

            if(!trackCheats) {
                itemView.findViewById(R.id.cheat_container).setVisibility(View.GONE);
            } if(!trackWater) {
                itemView.findViewById(R.id.water_container).setVisibility(View.GONE);
            }
        }

    }
}
