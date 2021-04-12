
package nscc.capstone.scrapbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import android.content.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class BattleActivity extends AppCompatActivity {

    // Constants
    final int NUM_PHOTOS = 9;
    final int NUM_IMG_RESOURCES = 48;

    // Controls
    TextView textViewPlayerWins, textViewComputerWins, textViewBattleVS;
    ImageView imageViewPlayerPhoto, imageViewComputerPhoto;
    Button btnGoToScore;

    // Animations
    Animation scaleUp, scaleDown, slideIn, slideIn2, slideOut, wave;

    // Animators and AnimatorSets
    ObjectAnimator playerAnimator, computerAnimator;
    AnimatorSet pictureSet = new AnimatorSet();

    // AI Images
    Random random = new Random();
    ArrayList<String> aiImages = new ArrayList<>();

    //Instantiating our ColorChooser class, and calling the DetermineColor() method on both the
    //player and CPU photos.
    ColorChooser colorChooser = new ColorChooser();

    //Creating a new ScoreKeeper instance to keep score.
    ScoreKeeper score = new ScoreKeeper();

    //Instantiating our RockPaperScissors object and an array to hold the win sequence
    RockPaperScissors rockPaperScissors = new RockPaperScissors();
    int[] winSequence = new int[NUM_PHOTOS];

    // Variables
    int loopCounter = 0;
    boolean singular = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        // Create an animation object from the animation resource folder
        slideIn = AnimationUtils.loadAnimation(this,R.anim.slide_in);
        slideIn2 = AnimationUtils.loadAnimation(this,R.anim.slide_in);
        slideOut = AnimationUtils.loadAnimation(this,R.anim.slide_out);
        wave = AnimationUtils.loadAnimation(this,R.anim.wave);
        scaleUp = AnimationUtils.loadAnimation(this,R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this,R.anim.scale_down);

        // Controls
        textViewPlayerWins = findViewById(R.id.textViewPlayerWins);
        textViewComputerWins = findViewById(R.id.textViewComputerWins);
        textViewBattleVS = findViewById(R.id.textViewBattleVS);
        imageViewPlayerPhoto = findViewById(R.id.imageViewPlayerPhoto);
        imageViewComputerPhoto = findViewById(R.id.imageViewComputerPhoto);
        btnGoToScore = findViewById(R.id.btnGoToScore);

        // Animators
        playerAnimator = ObjectAnimator.ofFloat(imageViewPlayerPhoto, "translationX", -1000f, 0f);
        computerAnimator = ObjectAnimator.ofFloat(imageViewComputerPhoto, "translationX", 1000f, 0f);
        playerAnimator.setDuration(1500);
        computerAnimator.setDuration(1500);
        pictureSet.playTogether(playerAnimator, computerAnimator);

        // Hide the winning text views
        textViewPlayerWins.setVisibility(View.INVISIBLE);
        textViewComputerWins.setVisibility(View.INVISIBLE);

        // Get the computer images and add them to the array list
        getComputerImages();

        // run the score at the beginning before doing the animations
        generateScore();

        // Start player image view to the first photo they chose
        imageViewPlayerPhoto.setImageBitmap(PhotoActivity.bitmapList.get(0));

        // Start computer photo to first in the list
        //Gets the resource ID of the computers photo
        int computerImageResourceID = getResources().getIdentifier(aiImages.get(0),
                "drawable", getApplicationContext().getApplicationInfo().packageName);

        //Set the image view at the start of the animation
        //Sets the computers image view to be the bitmap of the chosen computer photo
        imageViewComputerPhoto.setImageBitmap(getBitmapFromDrawable(computerImageResourceID));

        pictureSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

                // Displays what player won.
                switch (winSequence[loopCounter])
                {
                    case 0: // COMPUTER
                        textViewPlayerWins.setVisibility(View.INVISIBLE);
                        textViewComputerWins.setVisibility(View.VISIBLE);
                        break;
                    case 1: // PLAYER
                        textViewPlayerWins.setVisibility(View.VISIBLE);
                        textViewComputerWins.setVisibility(View.INVISIBLE);
                        break;
                    case 2: // TIE
                        textViewPlayerWins.setVisibility(View.VISIBLE);
                        textViewComputerWins.setVisibility(View.VISIBLE);
                        break;
                    default: // ERROR
                        textViewPlayerWins.setVisibility(View.INVISIBLE);
                        textViewComputerWins.setVisibility(View.INVISIBLE);
                        break;
                }

                //sets the imageview at the start of the animation
                imageViewPlayerPhoto.setImageBitmap(PhotoActivity.bitmapList.get(loopCounter));

                //Gets the resource ID of the computers photo
                int computerImageResourceID = getResources().getIdentifier(aiImages.get(loopCounter),
                        "drawable", getApplicationContext().getApplicationInfo().packageName);

                //Set the image view at the start of the animation
                //Sets the computers image view to be the bitmap of the chosen computer photo
                imageViewComputerPhoto.setImageBitmap(getBitmapFromDrawable(computerImageResourceID));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //increments the counter and starts next animation
                if (loopCounter < (NUM_PHOTOS-1)) {
                    loopCounter += 1;
                    pictureSet.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Unused
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // Unused
            }
        });

        // Listeners
        btnGoToScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Send the score to the score Activity
                Intent i = new Intent(BattleActivity.this, ScoreActivity.class); // Goto Score Activity
                i.putExtra("playerScore", score.getPlayerScore() );
                i.putExtra("computerScore", score.getComputerScore() );

                // Play Animation
                btnGoToScore.startAnimation(scaleUp);
                btnGoToScore.startAnimation(scaleDown);

                // Start the Score Activity
                startActivityForResult(i, 1);
            }
        });

    }//end onCreate

    @Override
    protected void onStart(){
        super.onStart();
        // Start first animations
        pictureSet.start();
    }//end onStart

    /* ---- Stubs for Activity Lifestyle Code ---- */
    @Override
    protected void onResume(){
        super.onResume();

        // Put onResume code here

        // Test
        // Toast notification on resume
        // Toast.makeText(TitleActivity.this,"On Resume",Toast.LENGTH_SHORT).show();

    }//end onResume

    @Override
    protected void onPause(){
        super.onPause();

        // Put onPause code here

        // Test
        // Toast notification on pause
        // Toast.makeText(TitleActivity.this,"On Pause",Toast.LENGTH_SHORT).show();

    }//end onPause

    @Override
    protected void onRestart(){
        super.onRestart();

        // Put onRestart code here

        // Test
        // Toast notification on restart
        // Toast.makeText(TitleActivity.this,"On Restart",Toast.LENGTH_SHORT).show();

    }//end onRestart

    @Override
    protected void onStop(){
        super.onStop();

        // Put onStop code here

        // Test
        // Toast notification on stop
        // Toast.makeText(TitleActivity.this,"On Stop",Toast.LENGTH_SHORT).show();

    }//end onStop

    @Override
    protected void onDestroy(){
        super.onDestroy();

        // Put onDestroy code here

        // Test
        // Toast notification on destroy
        // Toast.makeText(TitleActivity.this,"On Destroy",Toast.LENGTH_SHORT).show();

    }//end onDestroy

    public Bitmap getBundleImage(String bundleName){
        return BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra(bundleName),0,getIntent().getByteArrayExtra(bundleName).length);
    }

    public Bitmap getBitmapFromDrawable(int imageId)
    {
        return BitmapFactory.decodeResource(this.getResources(),imageId);
    }


    /**
     * Summary: Retrieve the image names for the computer and add them to the array list.
     */
    private void getComputerImages()
    {
        //Randomly get 9 photo names
        for (int i = 0; i < NUM_PHOTOS; i++){
            aiImages.add("img_" + random.nextInt(NUM_IMG_RESOURCES));
        }
    }

    /**
     * Summary: Will generate a score from the two bitmap lists.
     */
    private void generateScore()
    {
        for(int i = 0; i < NUM_PHOTOS; i++)
        {
            //Gets the resource ID of the computer photo
            int computerImageResourceID = getResources().getIdentifier(aiImages.get(i),
                    "drawable", getApplicationContext().getApplicationInfo().packageName);

            //DetermineColor() takes one parameter, the bitmap for the photo you want to test,
            //DetermineColor() will return a 1 if the photo is 'red', 2 for 'green', 3 for 'blue'
            int playerColorResult = colorChooser.DetermineColor(PhotoActivity.bitmapList.get(i));
            int computerColorResult = colorChooser.DetermineColor(getBitmapFromDrawable(computerImageResourceID));

            //DetermineWinner() takes in two ints, the results of the player and computers color, and compares them.
            //Returns a 0 if the CPU wins, 1 if the player wins, 2 if it's a tie, and a -1 if there was an error
            int versusResult = rockPaperScissors.DetermineWinner(playerColorResult,computerColorResult);

            // Adjust the score
            switch (versusResult)
            {
                case 0: // COMPUTER WINS
                    score.setComputerScore(score.getComputerScore()+1);
                    winSequence[i] = 0;
                    break;
                case 1: // PLAYER WINS
                    score.setPlayerScore(score.getPlayerScore()+1);
                    winSequence[i] = 1;
                    break;
                case 2: // TIE
                    winSequence[i] = 2;
                    break;
                default: // ERROR
                    winSequence[i] = -1;
                    break;
            }
        }
    }
}