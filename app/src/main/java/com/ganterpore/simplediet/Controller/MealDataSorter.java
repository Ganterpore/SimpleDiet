package com.ganterpore.simplediet.Controller;

import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.RequiresApi;

import com.ganterpore.simplediet.Model.Meal;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MealDataSorter {
    public static final String TAG = "MealDataSorter";
    SparseArray<List<Meal>> mealsByDay = new SparseArray<>();

    public MealDataSorter(List<DocumentSnapshot> data) {
        //if running new enough android, sort the data by day
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.sort(new Comparator<DocumentSnapshot>() {
                @Override
                public int compare(DocumentSnapshot o1, DocumentSnapshot o2) {
                    double rawScore =  o1.getDouble("day") - o2.getDouble("day");
                    if(rawScore > 0) {return -1;}
                    else if(rawScore < 0) {return 1;}
                    else {return 0;}
                }
            });
        }
        //seperate the data into different slots for each day
        for(DocumentSnapshot mealData : data) {
            Meal meal = mealData.toObject(Meal.class);
            meal.setId(mealData.getId());
            if(meal == null) {
                continue;
            }
            int daysAgo = getDaysAgo(meal);
            //adding the meal to the list for the correct daysAgo
            if(mealsByDay.get(daysAgo)==null) {
                mealsByDay.put(daysAgo, new ArrayList<Meal>());
            }
            mealsByDay.get(daysAgo).add(meal);
        }
    }

    public List<Meal> getMealsOnDay(int daysAgo) {
        return mealsByDay.get(daysAgo, new ArrayList<Meal>());
    }

    /**
     * Gets how many days ago a meal was created
     */
    private int getDaysAgo(Meal meal) {
        long changedDay = meal.getDay();
        Date changedDayStart = getStartOfDay(new Date(changedDay));
        long msDiff = System.currentTimeMillis() - changedDayStart.getTime();
        return (int) TimeUnit.MILLISECONDS.toDays(msDiff);
    }

    /**
     * get a date representing the start time of a given day
     * @param date, the date with which you want the start time of the day from
     * @return the date at the start of the given day
     */
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTime();
    }
}
