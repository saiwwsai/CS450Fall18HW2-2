package edu.stlawu.montyhall;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static edu.stlawu.montyhall.MainFragment.PREF_NAME;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment {

    private ImageButton door1 = null;
    private ImageButton door2 = null;
    private ImageButton door3 = null;

    private Handler handler = new Handler();
    private int carDoor = -1;
    private int showDoor = 0;
    private int doorClicked = 0;

    private TextView prompt = null;
    private TextView prompt2 = null;
    private TextView win = null;
    private int winNum = 0;
    private TextView loss = null;
    private int lossNum = 0;
    private TextView total = null;
    private int totalNum = 0;

    private AlertDialog.Builder builder = null;

    private String result = null;

    private AudioAttributes aa = null;
    private SoundPool soundPool = null;
    private int goatSound = 0;
    private int carSound = 0;
    private int clickSound = 0;
    private int popupSound = 0;
    private int countdownSound = 0;
    private int magicSound = 0;
    private Animation bouncing = null;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View gameView = inflater.inflate(R.layout.fragment_game, container, false);

        // Image Button activities - doors
        this.door1 = gameView.findViewById(R.id.door1);
        this.door2 = gameView.findViewById(R.id.door2);
        this.door3 = gameView.findViewById(R.id.door3);
        this.prompt = gameView.findViewById(R.id.prompt);
        this.prompt2 = gameView.findViewById(R.id.prompt2);
        this.win = gameView.findViewById(R.id.wins);
        this.loss = gameView.findViewById(R.id.losses);
        this.total = gameView.findViewById(R.id.totals);

        bouncing = AnimationUtils.loadAnimation(getActivity(), R.anim.bouncing);
        aa = new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
        soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(aa).build();
        goatSound = soundPool.load(getContext(), R.raw.goat, 1);
        carSound = soundPool.load(getContext(), R.raw.car, 1);
        clickSound = soundPool.load(getContext(), R.raw.click, 1);
        popupSound = soundPool.load(getContext(), R.raw.popup, 1);
        countdownSound = soundPool.load(getContext(), R.raw.countdown, 1);
        magicSound = soundPool.load(getContext(), R.raw.magic, 1);

        SharedPreferences prefer = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        //new game
        if (prefer.getBoolean("NEW_CLICKED", false)) {
            winNum = 0;
            lossNum = 0;
            totalNum = 0;
            firstClick();
            prefer.edit().putBoolean("NEW_CLICKED", false).apply();
        } else {
            // continue
            this.winNum = prefer.getInt("WINS", 0);
            this.lossNum = prefer.getInt("LOSSES", 0);
            this.totalNum = winNum + lossNum;

            carDoor = prefer.getInt("carDoor", -1);
            showDoor = prefer.getInt("showDoor", 0);
            doorClicked = prefer.getInt("doorClicked", 0);


            // todo cannot back up at the middle of popups

            result = prefer.getString("result", null);
            firstClick();
        }
        win.setText(Integer.toString(winNum));
        loss.setText(Integer.toString(lossNum));
        total.setText(Integer.toString(totalNum));

        return gameView;
    }


    public void firstClick() {


        Random carRandom = new Random();
        carDoor = carRandom.nextInt(3); // doors[car] is the door with car

        prompt.setVisibility(View.VISIBLE);
        prompt2.setVisibility(View.INVISIBLE);
        soundPool.play(magicSound, 1, 1, 1, 0, 1f);
        door1.startAnimation(bouncing);
        door2.startAnimation(bouncing);
        door3.startAnimation(bouncing);


        // set onClickListener
        door1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door1.setImageLevel(1);
                doorClicked = 1;
                // cannot click on doors until second click
                blockDoors();
                showGoat();
                prompt.setVisibility(View.INVISIBLE);
            }
        });

        door2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door2.setImageLevel(1);
                doorClicked = 2;
                blockDoors();
                showGoat();
                prompt.setVisibility(View.INVISIBLE);
            }
        });

        door3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door3.setImageLevel(1);
                doorClicked = 3;
                blockDoors();
                showGoat();
                prompt.setVisibility(View.INVISIBLE);
            }
        });
    }

  //todo  onpause save score

    public void showGoat() {
        // Toast indicating another door is open
        Toast.makeText(getActivity(), "Another Door is Opening!", Toast.LENGTH_LONG).show();


        switch (doorClicked) {
            case 1:
                // user chose door 1
                // if the car door was clicked, open door2 or 3 randomly
                if (carDoor == 0) {
                    Random openRandom = new Random();
                    int n = openRandom.nextInt(2); // 0 or 1
                    if (n == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door2.setImageLevel(3); // door2 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 2;
                                //door2.setEnabled(false);
                                showPopup();
                            }
                        }, 1000);
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door3.setImageLevel(3); // door3 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 3;
                                //  door3.setEnabled(false);
                                showPopup();
                            }
                        }, 1000);
                    }
                }
                // door 2 is car door, open 3
                else if (carDoor == 1) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door3.setImageLevel(3); // door3 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 3;
                            //   door3.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                } else if (carDoor == 2) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door2.setImageLevel(3); // door2 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 2;
                            //   door2.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                }
                break;
            case 2:
                // user chose door 2
                // door 2 is car door
                if (carDoor == 1) {
                    Random openRandom = new Random();
                    int n = openRandom.nextInt(2); // 0 or 1
                    if (n == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door1.setImageLevel(3); // door1 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 1;
                                //     door1.setEnabled(false);
                                showPopup();
                            }
                        }, 3000);
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door3.setImageLevel(3); // door3 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 3;
                                //    door3.setEnabled(false);
                                showPopup();
                            }
                        }, 3000);
                    }
                }
                // door 1 is car door, open 3
                else if (carDoor == 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door3.setImageLevel(3); // door3 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 3;
                            //     door3.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                } else if (carDoor == 2) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door1.setImageLevel(3); // door1 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 1;
                            //     door1.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                }
                break;
            case 3:
                // door 3 was clicked
                // door3 is car
                if (carDoor == 2) {
                    Random openRandom = new Random();
                    int n = openRandom.nextInt(2); // 0 or 1
                    if (n == 0) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door2.setImageLevel(3); // door2 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 2;
                                //      door2.setEnabled(false);
                                showPopup();
                            }
                        }, 3000);
                    } else {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // open goat door after 3s
                                door1.setImageLevel(3); // door1 open
                                soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                showDoor = 1;
                                //        door1.setEnabled(false);
                                showPopup();
                            }
                        }, 3000);
                    }
                }
                // door 2 is car door, open 1
                else if (carDoor == 1) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door1.setImageLevel(3); // door1 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 1;
                            //      door1.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                } else if (carDoor == 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // open goat door after 3s
                            door2.setImageLevel(3); // door1 open
                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                            showDoor = 2;
                            //      door2.setEnabled(false);
                            showPopup();
                        }
                    }, 3000);
                }
        }

    }

    public void showPopup() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                builder =
                        new AlertDialog.Builder(getActivity());
                soundPool.play(popupSound, 1, 1, 1, 0, 1f);
                // cannot close it by click outside
                builder.setCancelable(false);
                builder.setTitle(R.string.door_switch_title); //"Switch to Another Door"
                builder.setMessage(R.string.door_switch_text); //"Do you want to switch door"
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {  //"YES"
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                                showClosedDoors();
                                secondClick();
                            }
                        });
                builder.setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {  //No
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                                finalResult();
                            }
                        });
                builder.show();

            }
        }, 1000);
    }

    public void secondClick() {
        prompt2.setVisibility(View.VISIBLE);

        // user change doors
        this.door1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door1.setImageLevel(1);
                prompt.setVisibility(View.INVISIBLE);
                doorClicked = 1;
                blockDoors();
                finalResult();
                prompt2.setVisibility(View.INVISIBLE);
            }
        });

        this.door2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door2.setImageLevel(1);
                prompt.setVisibility(View.INVISIBLE);
                doorClicked=2;
                blockDoors();
                finalResult();
                prompt2.setVisibility(View.INVISIBLE);
            }
        });
        this.door3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(clickSound, 1, 1, 1, 0, 1f);
                // change image to chosen door
                door3.setImageLevel(1);
                prompt.setVisibility(View.INVISIBLE);
                doorClicked = 3;
                blockDoors();
                finalResult();
                prompt2.setVisibility(View.INVISIBLE);
            }
        });

    }

    public void showClosedDoors() {

        // if a door neither got clicked, nor got showed,
        // it is the only one can be shown

        blockDoors();
        door1.startAnimation(bouncing);
        door2.startAnimation(bouncing);
        door3.startAnimation(bouncing);

        if (doorClicked!=1 && showDoor != 1) {
            door1.setEnabled(true);
            door1.setImageLevel(0);
        }
        if (doorClicked!=2 && showDoor != 2) {
            door2.setEnabled(true);
            door2.setImageLevel(0);
        }
        if (doorClicked!=3 && showDoor != 3) {
            door3.setEnabled(true);
            door3.setImageLevel(0);
        }
    }

    @Override
    public void onPause() {
        // save state
        SharedPreferences prefer = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);


        prefer.edit().putInt("WINS", winNum).apply();
        prefer.edit().putInt("LOSSES", lossNum).apply();

        prefer.edit().putInt("carDoor", carDoor).apply();
        prefer.edit().putInt("doorClicked", doorClicked).apply();


        prefer.edit().putString("result", result).apply();

        prefer.edit().putBoolean("CONTINUE_CLICKED",false).apply();
        prefer.edit().putBoolean("NEW_CLICKED",false).apply();
        super.onPause();
    }

    public void finalResult() {
        blockDoors();
        // show car or goat according to final doorClicked
        // final choice is door 1
        if (doorClicked == 1) {
            //countdown show behind
            countDown(door1);
        }

        // door2 is final choice
        if (doorClicked == 2) {
            //countdown show behind
            countDown(door2);
        }
        // door3 is final choice
        if (doorClicked == 3) {
            countDown(door3);
        }

    }

    public void countDown(final ImageButton door) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                door.setImageLevel(4);
                soundPool.play(countdownSound, 1, 1, 1, 0, 1f);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        door.setImageLevel(5);
                        soundPool.play(countdownSound, 1, 1, 1, 0, 1f);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                door.setImageLevel(6);
                                soundPool.play(countdownSound, 1, 1, 1, 0, 1f);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (carDoor == getDoorNum(door)) {
                                            door.setImageLevel(2);
                                            soundPool.play(carSound, 3, 3, 1, 0, 1f);
                                            result = "win";
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    playAgain();
                                                    updateData();
                                                }
                                            }, 3000);

                                        } else {
                                            door.setImageLevel(3);
                                            soundPool.play(goatSound, 1, 1, 1, 0, 1f);
                                            result = "loss";
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    playAgain();
                                                    updateData();
                                                }
                                            }, 1000);
                                        }

                                    }
                                }, 1000);

                            }
                        }, 1000);
                    }
                }, 1000);
            }
        }, 1000);
    }

    public void playAgain() {
        if (result.equals("win")) {

            builder =
                    new AlertDialog.Builder(getActivity());
            soundPool.play(popupSound, 1, 1, 1, 0, 1f);
            builder.setCancelable(false);
            builder.setTitle(R.string.aCar);
            builder.setMessage(R.string.you_win);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {  //"YES"
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // restart the game
                            reset();
                        }
                    });
            builder.setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {  //No
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // back to home page
                            SharedPreferences.Editor prefer =
                                    getActivity().getSharedPreferences(
                                            PREF_NAME, MODE_PRIVATE).edit();
                              prefer.putBoolean("BackToHomePage", true).apply();
                            Intent intent = new Intent(
                                    getActivity(), MainActivity.class);
                            getActivity().startActivity(intent);
                        }
                    });
            builder.show();

        } else {
           builder =
                    new AlertDialog.Builder(getActivity());
            soundPool.play(popupSound, 1, 1, 1, 0, 1f);
            builder.setCancelable(false);
            builder.setTitle(R.string.aGoat);
            builder.setMessage(R.string.you_loss);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {  //"YES"
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // restart the game
                            reset();
                        }
                    });
            builder.setNegativeButton(R.string.no,
                    new DialogInterface.OnClickListener() {  //No
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // back to home page
                            SharedPreferences.Editor prefer =
                                    getActivity().getSharedPreferences(
                                            PREF_NAME, MODE_PRIVATE).edit();
                            prefer.putBoolean("BackToHomePage", true).apply();
                            Intent intent = new Intent(
                                    getActivity(), MainActivity.class);
                            getActivity().startActivity(intent);
                            // todo back to home then back to game cannot save grades
                        }
                    });
            builder.show();

        }

    }


    public void updateData() {
        if (result.equals("win")) {
            winNum++;
            totalNum++;
            win.setText(Integer.toString(winNum));
            total.setText(Integer.toString(totalNum));
        } else {
            lossNum++;
            totalNum++;
            loss.setText(Integer.toString(lossNum));
            total.setText(Integer.toString(totalNum));
        }

   /*     SharedPreferences.Editor prefer =
                getActivity().getSharedPreferences(
                        PREF_NAME, MODE_PRIVATE).edit();
        this.winNum = prefer.getInt("WINS", winNum);
        this.lossNum = prefer.getInt("LOSSES", lossNum);

        this.totalNum = winNum + lossNum;
        */

    }
    public void reset() {
        door1.setEnabled(true);
        door2.setEnabled(true);
        door3.setEnabled(true);

        door1.setImageLevel(0);
        door2.setImageLevel(0);
        door3.setImageLevel(0);

        doorClicked = 0;

        firstClick();
        // todo back to home then back to game cannot save grades
        SharedPreferences.Editor prefer =
                getActivity().getSharedPreferences(
                        PREF_NAME, MODE_PRIVATE).edit();
        prefer.putInt("WINS", winNum).apply();
        prefer.putInt("LOSSES", lossNum).apply();

        this.totalNum = winNum + lossNum;

    }


    public void blockDoors() {
        door1.setEnabled(false);
        door2.setEnabled(false);
        door3.setEnabled(false);
    }


    public int getDoorNum(ImageButton door) {
        if (door.equals(door1)) {
            return 1;
        }
        if (door.equals(door2)) {
            return 2;
        }
        if (door.equals(door3)) {
            return 3;
        } else {
            return 0;
        }
    }


    @Override
    public void onDestroy() {
        // save state
        SharedPreferences prefer = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);


        prefer.edit().putInt("WINS", winNum).apply();
        prefer.edit().putInt("LOSSES", lossNum).apply();

        prefer.edit().putInt("carDoor", carDoor).apply();
        prefer.edit().putInt("doorClicked", doorClicked).apply();


        prefer.edit().putString("result", result).apply();

        prefer.edit().putBoolean("CONTINUE_CLICKED",false).apply();
        prefer.edit().putBoolean("NEW_CLICKED",false).apply();

        super.onDestroy();
    }
}
