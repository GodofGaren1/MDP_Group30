package com.example.mdp_group30.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.mdp_group30.MainActivity;
import com.example.mdp_group30.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ControlFragment is a Fragment class that displays the control buttons and timers for the robot in the
 * MainActivity.

 * It contains two timers for image recognition and fastest car simulation, and button listeners
 * to control the robot's movement.
 */
public class ControlFragment extends Fragment {
    /**
     * A string constant used for logging purposes.
     */
    private static final String TAG = "ControlFragment";
    /**
     * The MainActivity instance that contains this fragment.
     */
    private final MainActivity mainActivity;

    private long imgRecTime, fastestCarTime;
    private SharedPreferences sharedPreferences;
    private ToggleButton imgRecBtn, fastestCarBtn;
    private TextView imgRecText, fastestCarText, robotStatusText;
    private GridMap gridMap;
    private int[] curCoord;
    private String direction;

    /**
     * Creates an instance of ControlFragment with the specified MainActivity instance.
     *
     * @param main the MainActivity instance that contains this fragment
     */
    public ControlFragment(MainActivity main) {
        this.mainActivity = main;
    }

    /**
     * The Handler used for timing the image recognition timer and fastest car timer.
     */
    public static Handler timerHandler = new Handler();

    /**
     * The Runnable for the image recognition timer.
     */
    public Runnable imgRecTimer = new Runnable() {
        @Override
        public void run() {
            long msTime = System.currentTimeMillis() - imgRecTime;
            int sTime = (int) (msTime / 1000);
            int minuteTime = sTime / 60;
            sTime = sTime % 60;

            if (! mainActivity.imgRecTimerFlag) {
                imgRecText.setText(String.format(Locale.US, "%02d:%02d", minuteTime, sTime));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    /**
     * The Runnable for the fastest car timer.
     */
    public Runnable fastestCarTimer = new Runnable() {
        @Override
        public void run() {
            long msTime = System.currentTimeMillis() - fastestCarTime;
            int sTime = (int) (msTime / 1000);
            int minuteTime = sTime / 60;
            sTime = sTime % 60;

            if (!mainActivity.fastestCarTimerFlag) {
                fastestCarText.setText(String.format(Locale.US,"%02d:%02d", minuteTime,
                        sTime));
                timerHandler.postDelayed(this, 500);
            }
        }
    };

//    public List<List<String>> splitStringToList(String input) {
//        // Step 1: Split by '|' but exclude empty strings
//        List<List<String>> obstacleList = new ArrayList<>();
//        String[] parts = input.split("|");
//
//        // Step 2: Process each part
//        for (String part : parts) {
//            if (!part.isEmpty()) { // Avoid empty parts caused by leading and trailing '|'
//                // Step 3: Split the part by ',' to get individual values
//                String[] values = part.split(",");
//                List<String> obstacle = new ArrayList<>();
//                for (String value : values) {
//                    obstacle.add(value);
//                }
//                // Add to the outer list
//                obstacleList.add(obstacle);
//            }
//        }
//
//        return obstacleList;
//    }
public List<List<String>> splitStringToList(String input) {
    // Step 1: Split by '|' but exclude empty strings
    List<List<String>> obstacleList = new ArrayList<>();
    String[] parts = input.split("\\|");

    // Step 2: Process each part
    for (String part : parts) {
        if (!part.isEmpty()) { // Avoid empty parts caused by leading and trailing '|'
            // Log the part being processed
            Log.d(TAG, "Processing part: " + part);

            // Step 3: Split the part by ',' to get individual values
            String[] values = part.split(",");

            // Create a new obstacle list
            List<String> obstacle = new ArrayList<>();

            // Log the values being added to the obstacle
            for (String value : values) {
                Log.d(TAG, "Adding value: " + value);
                obstacle.add(value);
            }

            // Add to the outer list
            obstacleList.add(obstacle);
        }
    }

    Log.d(TAG, "Final obstacle list: " + obstacleList);

    return obstacleList;
}

    public void sendStartMessage(String cat, String value){
        JSONObject messageObject = new JSONObject();

        try {
            // Set the "cat" value
            messageObject.put("cat", cat);
            messageObject.put("value", value);
            String messageString = messageObject.toString();

            this.mainActivity.sendMessage(messageString);
        }
         catch (Exception e) {
            e.printStackTrace(); // Handle exception if any
        }
    }
    public void printObstacleList(List<List<String>> obstacleList) {
        String TAG = "ObstacleListDebug"; // Log tag to filter in Logcat

        // Check if the list is not empty
        if (obstacleList.isEmpty()) {
            Log.d(TAG, "Obstacle list is empty");
            return;
        }

        // Iterate over each sublist (obstacle)
        for (int i = 0; i < obstacleList.size(); i++) {
            List<String> obstacle = obstacleList.get(i);
            Log.d(TAG, "Obstacle " + i + ": " + obstacle);
        }
    }

    public void sendObstacleMessage(String cat, List<List<String>> obstacleList) {
        try {
            // Create JSON object for sending
            JSONObject messageObject = new JSONObject();

            // Set the "cat" value
            messageObject.put("cat", cat);

            // Create the value object containing obstacles and mode
            JSONObject valueObject = new JSONObject();

            // Create an array of obstacles
            JSONArray obstaclesArray = new JSONArray();

            //for loop
            printObstacleList(obstacleList);
            int length = obstacleList.size();
            for (int i = 0; i < length; i++) {
                if (obstacleList.get(i).size() > 1){
                    int temp = 0;
                    switch(obstacleList.get(i).get(3)){
                        case "N":
                            temp = 0;
                            break;
                        case "S":
                            temp = 1;
                            break;
                        case "E":
                            temp = 2;
                            break;
                        case "W":
                            temp = 3;
                            break;
                        default:
                            break;
                    }
                    JSONObject obstacle1 = new JSONObject();
                    obstacle1.put("x", Integer.parseInt(obstacleList.get(i).get(0)));
                    obstacle1.put("y", Integer.parseInt(obstacleList.get(i).get(1)));
                    obstacle1.put("id", Integer.parseInt(obstacleList.get(i).get(3)));
                    obstacle1.put("d", (int) temp);
                    obstaclesArray.put(obstacle1);
                }
                else{
                    Log.e("Error", "Invalid obstacle data at index " + i);
                }
            }

            // Put obstacles and mode into the value object
            valueObject.put("obstacles", obstaclesArray);
            valueObject.put("mode", "0");

            // Add the value object to the main message
            messageObject.put("value", valueObject);

            // Convert the JSON object to a string
            String messageString = messageObject.toString();

            // Send the message
            this.mainActivity.sendMessage(messageString);

        } catch (Exception e) {
            e.printStackTrace(); // Handle exception if any
        }
    }
    /**
     * Initializes the fragment.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater           the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container          the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the saved instance state
     * @return the View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate
        View root = inflater.inflate(R.layout.activity_control, container, false);

        // get shared preferences
        this.sharedPreferences = requireActivity()
                .getSharedPreferences("Shared Preferences", Context.MODE_PRIVATE);

        // initialize all buttons and text views
        ImageButton forwardBtn = this.mainActivity.getUpBtn();
        ImageButton rightBtn = this.mainActivity.getRightBtn();
        ImageButton backBtn = this.mainActivity.getDownBtn();
        ImageButton leftBtn = this.mainActivity.getLeftBtn();
        ImageButton imgRecResetBtn = root.findViewById(R.id.exploreResetImageBtn2);
        ImageButton fastestCarResetBtn = root.findViewById(R.id.fastestResetImageBtn2);
        this.imgRecText = root.findViewById(R.id.exploreTimeTextView2);
        this.fastestCarText = root.findViewById(R.id.fastestTimeTextView2);
        this.imgRecBtn = root.findViewById(R.id.exploreToggleBtn2);
        this.fastestCarBtn = root.findViewById(R.id.fastestToggleBtn2);
        this.robotStatusText = this.mainActivity.getRobotStatusText();

        // default time is 0
        this.fastestCarTime = 0;
        this.imgRecTime = 0;

        // need to get the gridMap to call the private methods
        this.gridMap = this.mainActivity.getGridMap();

        // button listeners. Runs when the buttons are pressed
        forwardBtn.setOnClickListener(view -> {
            // only reacts when robot is placed on gridmap
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                // handles translation based on existing direction
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] + 1}, 0);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1]}, 0);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] - 1}, 0);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1]}, 0);
                        break;
                }
                // refreshes the UI displayed coordinate of robot
                this.mainActivity.refreshCoordinate();
                this.mainActivity.sendMessage("forward");
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        rightBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 4, this.curCoord[1] + 2}, -90);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 2, this.curCoord[1] + 4}, -90);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 4, this.curCoord[1] - 2}, -90);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 2, this.curCoord[1] - 4}, -90);
                        break;
                }

                this.mainActivity.refreshCoordinate();
                this.mainActivity.sendMessage("right");
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        backBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] - 1}, 0);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1]}, 0);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0], this.curCoord[1] + 1}, 0);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1]}, 0);
                        break;
                }
                this.mainActivity.refreshCoordinate();
                this.mainActivity.sendMessage("back");
            }
            else {
                this.showToast("Please place robot on map to begin");
            }
        });

        leftBtn.setOnClickListener(view -> {
            if (this.gridMap.getCanDrawRobot()) {
                this.curCoord = this.gridMap.getCurCoord();
                this.direction = this.gridMap.getRobotDirection();
                switch (this.direction) {
                    case "up":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 4, this.curCoord[1] + 1}, 90);
                        break;
                    case "left":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] - 1, this.curCoord[1] - 4}, 90);
                        break;
                    case "down":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 4, this.curCoord[1] - 1}, 90);
                        break;
                    case "right":
                        this.gridMap.moveRobot(new int[]{this.curCoord[0] + 1, this.curCoord[1] + 4}, 90);
                        break;
                }
                this.mainActivity.refreshCoordinate();
                this.mainActivity.sendMessage("left");
            }
            else
                this.showToast("Please place robot on map to begin");
        });

        this.imgRecBtn.setOnClickListener(v -> {
            // changed from STOP to START (i.e. done with challenge)
            if (this.imgRecBtn.getText().equals("START")) {
                this.showToast("Image Recognition Completed!!");
                this.robotStatusText.setText(R.string.img_rec_stop);
                timerHandler.removeCallbacks(this.imgRecTimer);
            }
            // changed from START to STOP (i.e. started challenge)
            else if (this.imgRecBtn.getText().equals("STOP")) {
                this.mainActivity.imgRecTimerFlag = false;
                this.showToast("Image Recognition Started!!");
                String getObsPos = this.gridMap.getAllObstacles();
                List<List<String>> obstacleList = splitStringToList(getObsPos);
//                getObsPos = "OBS|" + getObsPos;
//                this.mainActivity.sendMessage(getObsPos);
//                this.mainActivity.sendMessage("{"cat": "obstacles", "value": {"obstacles": [{"x": 5, "y": 10, "id": 1, "d": 2}, {"x": 10, "y": 15, "id": 1, "d": 3}], "mode": "0"}}");
                sendObstacleMessage("obstacles", obstacleList);
                this.robotStatusText.setText(R.string.img_rec_start);
                this.imgRecTime = System.currentTimeMillis();
                timerHandler.postDelayed(imgRecTimer, 0);
            }
        });

        this.fastestCarBtn.setOnClickListener(v -> {
            // changed from STOP to START (i.e., challenge completed)
            if (this.fastestCarBtn.getText().equals("START")) {
                this.showToast("Fastest Car Stopped!");
                this.robotStatusText.setText(R.string.fastest_car_stop);
                timerHandler.removeCallbacks(fastestCarTimer);
            }
            // changed from START to STOP (i.e., challenge started)
            else if (fastestCarBtn.getText().equals("STOP")) {
                this.showToast("Fastest Car started!");
//                this.mainActivity.sendMessage("STM|Start");
                sendStartMessage("control", "start");

                this.mainActivity.fastestCarTimerFlag = false;
                this.robotStatusText.setText(R.string.fastest_car_start);
                this.fastestCarTime = System.currentTimeMillis();
                timerHandler.postDelayed(fastestCarTimer, 0);
            }
        });

        imgRecResetBtn.setOnClickListener(v -> {
            this.showToast("Resetting image recognition challenge timer...");
            this.imgRecText.setText(R.string.timer_default_val);
            this.robotStatusText.setText(R.string.robot_status_na);
            if (this.imgRecBtn.isChecked())
                this.imgRecBtn.toggle();
            timerHandler.removeCallbacks(imgRecTimer);
        });

        fastestCarResetBtn.setOnClickListener(view -> {
            this.showToast("Resetting fastest car challenge timer...");
            this.fastestCarText.setText(R.string.timer_default_val);
            this.robotStatusText.setText(R.string.robot_status_na);
            if (this.fastestCarBtn.isChecked()){
                this.fastestCarBtn.toggle();
            }
            timerHandler.removeCallbacks(fastestCarTimer);
        });

        return root;
    }

    /**
     * Method to display debug message
     * @param message The custom message shown in debugging
     */
    private void debugMessage(String message) {
        Log.d(TAG, message);
    }

    /**
     * Displays a toast with message on the UI
     * @param message The displayed message
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}