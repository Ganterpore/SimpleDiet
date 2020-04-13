package com.ganterpore.simplediet.View.Activities;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.ganterpore.simplediet.Controller.BasicDietController;
import com.ganterpore.simplediet.Controller.DietController;
import com.ganterpore.simplediet.Controller.WeeklyIntake;
import com.ganterpore.simplediet.R;
import com.ganterpore.simplediet.View.ItemViews.CompletableItemView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class HistoryActivity extends AppCompatActivity {
    public static final String TAG = "HistoryActivity";
    DietController dietController;
    WeeklyIntake week1;
    WeeklyIntake week2;
    WeeklyIntake week3;
    WeeklyIntake week4;

    RecyclerView weeksHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dietController = BasicDietController.getInstance();
        week1 = dietController.getWeeksIntake(0);
        week2 = dietController.getWeeksIntake(1);
        week3 = dietController.getWeeksIntake(2);
        week4 = dietController.getWeeksIntake(3);
        setMonthlyView();

        weeksHistory = findViewById(R.id.weeks_history);
        weeksHistory.setAdapter(new WeekHistoryAdapter(this, 4));
        weeksHistory.getAdapter().notifyDataSetChanged();
    }

    /**
     * Sets up all the values of the monthly view
     */
    private void setMonthlyView() {
        final int SCALE_FACTOR = 100; //how much to scale the progress bars by (to allow more granularity)
        NumberFormat df = new DecimalFormat("##.##"); //format to show all decimal strings
        View monthlyContainer = findViewById(R.id.monthly_container);

        //get the text views from the main activity
        TextView monthlyVegTV = findViewById(R.id.monthly_veges_intake);
        TextView monthlyProteinTV = findViewById(R.id.monthly_protein_intake);
        TextView monthlyDairyTV = findViewById(R.id.monthly_dairy_intake);
        TextView monthlyGrainTV = findViewById(R.id.monthly_grain_intake);
        TextView monthlyFruitTV = findViewById(R.id.monthly_fruit_intake);
        TextView monthlyWaterTV = findViewById(R.id.monthly_water_intake);
        TextView monthlyCaffeineTV = findViewById(R.id.monthly_caffeine_count);
        TextView monthlyAlcoholTV = findViewById(R.id.monthly_alcohol_count);
        TextView monthlyCheatTV = findViewById(R.id.monthly_cheat_count);

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

        //refreshing the other views

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
            return 4;
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
        int position;

        WeekHistoryViewHolder(Activity activity, @NonNull View itemView, WeekHistoryAdapter adapter) {
            super(itemView);
            this.itemView = itemView;
            this.activity = activity;
            this.adapter = adapter;
        }

        public void build(final int position) {
            this.position = position;
            //get the correct week from position
            WeeklyIntake week;
            switch (position) {
                case 0:
                    week = week1;
                    break;
                case 1:
                    week = week2;
                    break;
                case 2:
                    week = week3;
                    break;
                case 3:
                    week = week4;
                    break;
                default:
                    return;
            }

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

            //creating a list of the meals eaten that day
//            RecyclerView mealsList = itemView.findViewById(R.id.meals_list);
//            mealsList.setAdapter(new MealHistoryDisplay.MealsAdapter(activity, day.getMeals()));

            final View expandableView1 = itemView.findViewById(R.id.expanded_layout_1);
            final View expandableView2 = itemView.findViewById(R.id.expanded_layout_2);
            final ImageView dropdownButton = itemView.findViewById(R.id.dropdown_button);

            //making sure view is shown as it should be, either expanded or not
            if(adapter.nDaysAgoVisible.get(position, false)) {
                expandableView1.setVisibility(View.VISIBLE);
                expandableView2.setVisibility(View.VISIBLE);
                dropdownButton.setRotation(180);
            } else {
                expandableView1.setVisibility(View.GONE);
                expandableView2.setVisibility(View.GONE);
                dropdownButton.setRotation(0);
            }

            //creating functionality for the button that shows expands the card
            dropdownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(expandableView1.getVisibility() == View.GONE) {
                        expandableView1.setVisibility(View.VISIBLE);
                        expandableView2.setVisibility(View.VISIBLE);
                        dropdownButton.animate().setDuration(200).rotation(180);
                        adapter.nDaysAgoVisible.append(position, true);
                        adapter.notifyItemChanged(position);
                    } else {
                        expandableView1.setVisibility(View.GONE);
                        expandableView2.setVisibility(View.GONE);
                        dropdownButton.animate().setDuration(200).rotation(0);
                        adapter.nDaysAgoVisible.append(position, false);
                        adapter.notifyItemChanged(position);
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
        }

    }
}
