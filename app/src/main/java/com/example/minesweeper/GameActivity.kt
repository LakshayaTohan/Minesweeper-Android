package com.example.minesweeper

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_game.*
import kotlin.random.Random

//Inherit class GameActivity from MainMenu to have access to its variables
class GameActivity : MainMenu() {
    var isMineLocation = BooleanArray(rows*columns){false}      //Store locations of mines
    var isFirstMove = true                                           //Store if the move made is the first one, making it safe
    var isFirstFlagMove = true                                       //Variable for holding if first move was placing a flag
    var counter = 0                                                  //Counter for setting buttons and corresponding IDs
    var leftEdge = IntArray(rows){0}                                 //Store IDs/Indices of buttons at the left edge (used in fun countAdjacentMines())
    var left: Int = 0                                                //Used for storing the IDs in var leftEdge
    var rightEdge = IntArray(rows){0}                                //Store IDs/Indices of buttons at the right edge (used in fun countAdjacentMines())
    var right: Int = columns-1                                       //Used for storing the IDs in var rightEdge
    var isScanned = BooleanArray(rows*columns){false}           //Store if a particular button ID has been scanned or not (used in fun countAdjacentMines())
    var movesLeft: Int = (rows*columns) - mines                      //Store number of moves left
    var isFlagged = BooleanArray(rows*columns){false}           //Store if a button of particular ID has been flagged
    var flagCount: Int = mines                                       //Store the number of flags remaining
    var timeElapsedSeconds = -1                                      //Store time elapsed in seconds
    var timeElapsedMinutes = 0                                       //Store time elapsed in minutes

    //Variable for counting the time elapsed since game started, using class CountDownTimer
    var countUpTimer = object : CountDownTimer(1000000, 1000){
        override fun onTick(millisUntilFinished: Long) {
            timeElapsedSeconds++                                                            //Incrementing value for every time interval (1000 milliseconds passed)
            if(timeElapsedSeconds == 60){                                                   //Logic flow for converting seconds to minutes
                timeElapsedMinutes++
                timeElapsedSeconds = 0
            }
            if(timeElapsedSeconds<10)
                timer.setText("${timeElapsedMinutes}:0${timeElapsedSeconds}")
            else
                timer.setText("${timeElapsedMinutes}:${timeElapsedSeconds}")               //Setting time value in the timer TextView
        }
        override fun onFinish() {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val flags = findViewById<TextView>(R.id.flags)               //Store flags TextView for displaying the flagCount
        flags.setText(flagCount.toString())
        playMinesweeper()                                            //Call driver function
        val timer = findViewById<TextView>(R.id.timer)               //Store timer TextView for displaying elapsed time
        timer.setText("0:00")
    }

    fun setupBoard(){                                                //Function for generating the mine field
        val params1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0
        )
        val params2 = LinearLayout.LayoutParams(                     //Store layout parameters
            0,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        params2.setMargins(0,0,0,0)

        for(i in 1..rows){
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.layoutParams = params1
            params1.weight  = 1.0F

            for(j in 1..columns){
                val button = Button(this)
                button.id = counter
                button.textSize = 18.0F
                button.layoutParams = params2
                params2.weight = 1.0F
                button.setBackgroundResource(R.drawable.block_closed)   //Set background resource of buttons initially

                button.setOnClickListener{
                    if (isFirstMove) {                                  //If condition for checking for first move
                        countUpTimer.start()                            //Start timer after the first tap
                        isFirstMove = false
                        if(isMineLocation[button.id] == true)           //If the location has a mine, change its location
                            replaceMine(button.id)
                    }
                    if(isMine(button.id)) {                             //If tapped button is a mine, change background and end game,
                        button.setBackgroundResource(R.drawable.mine)
                        gameEnd(true)                         //with game lost = true
                    }
                    else {
                        countAdjacentMines(button.id)                   //Function call to count mines around the tapped location
                    }
                }

                button.setOnLongClickListener{                          //Set onLongClickListener for putting flag,
                    if (isFirstFlagMove) {                              //Timer starts even if the first move is flag
                        countUpTimer.start()
                        isFirstFlagMove = false
                    }
                    setFlag(button.id)                                  //at the tapped button ID
                    true
                }

                linearLayout.addView(button)                    //Add button to the row
                counter++
            }
            mineField.addView(linearLayout)                     //Add row to the mine field
        }
    }

    fun placeMines(counter: Int){                               //Function for randomly placing mines
        var i = 0

        for (i in 0..counter-1)
            isMineLocation[i] = false

        while(i < mines){
            val random = Random.nextInt(0, counter)       //Random number generator, between 0 and counter
            if (isMineLocation[random]!=true) {                 //Store mine locations in isMineLocation, if there is no mine previously
                isMineLocation[random] = true
                i++
            }
        }
    }

    fun replaceMine(id: Int) {                                  //Function for replacing mine if first tap holds a mine, enabling Safe First Move
        isMineLocation[id] = false
        var mineReplaced = false
        while (!mineReplaced) {
            val random = Random.nextInt(0, counter)
            if(isMineLocation[random] != true && random != id)
            {
                isMineLocation[random] = true
                mineReplaced = true
            }
        }
    }
    fun isValid(id: Int): Boolean{                              //Function to check if the provided ID is valid (in range)
        return(id>=0 && id<counter)
    }

    fun isMine(id: Int): Boolean {                              //Function to check if the provided ID holds mine or not
        if(isMineLocation[id])
            return true
        return false
    }

    fun storeEdges(){                                           //Function for storing the IDs of buttons at extreme edges, used in countAdjacentMines
        for(i in 0..rows-1) {
            leftEdge[i] = left
            left+= columns
            rightEdge[i] = right
            right+= columns
        }
    }

    fun countAdjacentMines(id: Int): Unit{                                  //Function for counting the mines surrounding a particular tile
        val button = findViewById<Button>(id)
        var count = 0
        var onLeftEdge: Boolean = false
        var onRightEdge: Boolean = false

        for (i in 0..rows - 1){                                             //If statement to check if button is at left or right edge
            if (id == leftEdge[i])                                          //In order to prevent NW, W and SW scan
                onLeftEdge = true
            else if (id == rightEdge[i])                                    //In order to prevent NE, E and SE scan
                onRightEdge = true
        }

        //Scan for mine at the mentioned locations and increase the value of count
        //North
        if(isValid(id-columns) && isMineLocation[id-columns])
            count++
        //North-East
        if(isValid(id-columns+1) && isMineLocation[id-columns+1] && !onRightEdge)
            count++
        //East
        if(isValid(id+1) && isMineLocation[id+1] && !onRightEdge)
            count++
        //South-East
        if(isValid(id+columns+1) && isMineLocation[id+columns+1] && !onRightEdge)
            count++
        //South
        if(isValid(id+columns) && isMineLocation[id+columns])
            count++
        //South-West
        if(isValid(id+columns-1) && isMineLocation[id+columns-1] && !onLeftEdge)
            count++
        //West
        if(isValid(id-1) && isMineLocation[id-1] && !onLeftEdge)
            count++
        //North-West
        if(isValid(id-columns-1) && isMineLocation[id-columns-1] && !onLeftEdge)
            count++

        if (isScanned[id] == true) {                    //If ID is already scanned, return in order to prevent an endless loop
            return
        }

        isScanned[id] = true

        if (isFlagged[button.id]) {                     //If statement to recover flags on IDs which were marked, but didn't have mine
            flagCount++
            flags.setText(flagCount.toString())
        }

        if (count == 0){                                //If statement for uncover tiles if mine count is 0, using recursion
            //North
            if(isValid(id-columns) && !isMineLocation[id-columns])
                countAdjacentMines(id-columns)
            //North-East
            if(isValid(id-columns+1) && !isMineLocation[id-columns+1] && !onRightEdge)
                countAdjacentMines(id-columns+1)
            //East
            if(isValid(id+1) && !isMineLocation[id+1] && !onRightEdge)
                countAdjacentMines(id+1)
            //South-East
            if(isValid(id+columns+1) && !isMineLocation[id+columns+1] && !onRightEdge)
                countAdjacentMines(id+columns+1)
            //South
            if(isValid(id+columns) && !isMineLocation[id+columns])
                countAdjacentMines(id+columns)
            //South-West
            if(isValid(id+columns-1) && !isMineLocation[id+columns-1] && !onLeftEdge)
                countAdjacentMines(id+columns-1)
            //West
            if(isValid(id-1) && !isMineLocation[id-1] && !onLeftEdge)
                countAdjacentMines(id-1)
            //North-West
            if(isValid(id-columns-1) && !isMineLocation[id-columns-1] && !onLeftEdge)
                countAdjacentMines(id-columns-1)
        }
        button.isEnabled = false                                    //Set scanned and/or tapped buttons to disabled
        placeMineCount(id, count)
        movesLeft--
        if (movesLeft == 0)                                         //If all possible moves have been done, end game,
            gameEnd(false)                                //with gameLost = false
        else
            return
    }

    fun placeMineCount(id: Int, count: Int){                        //Function for placing drawables corresponding to mine count at an ID
        val button = findViewById<Button>(id)
        when(count){
            0 -> button.setBackgroundResource(R.drawable.mines_0)
            1 -> button.setBackgroundResource(R.drawable.mines_1)
            2 -> button.setBackgroundResource(R.drawable.mines_2)
            3 -> button.setBackgroundResource(R.drawable.mines_3)
            4 -> button.setBackgroundResource(R.drawable.mines_4)
            5 -> button.setBackgroundResource(R.drawable.mines_5)
            6 -> button.setBackgroundResource(R.drawable.mines_6)
            7 -> button.setBackgroundResource(R.drawable.mines_7)
            8 -> button.setBackgroundResource(R.drawable.mines_8)
        }
    }

    fun gameEnd(gameLost: Boolean){                                                                             //Function for game end, either by tapping mine or uncovering all possible tiles
        countUpTimer.cancel()                                                                                   //Stop the timer

        for (i in 0 until rows*columns) {                                                                       //Reveal the complete board
            val button = findViewById<Button>(i)
            if (isMineLocation[i]) {
                button.setBackgroundResource(R.drawable.mine)
            }
            button.isEnabled = false
        }

        if(gameLost)                                                                                            //Snackbar messages corresponding to win or lose
            Snackbar.make(gameScreen, "You have lost. Keep trying!", Snackbar.LENGTH_INDEFINITE).show()
        else
            Snackbar.make(gameScreen, "You have won. Excellent work!", Snackbar.LENGTH_INDEFINITE).show()
    }

    fun setFlag(id: Int){                                                                               //Function for setting flags at an ID, and updating flag count in flag TextView
        val button = findViewById<Button>(id)

        if(!isFlagged[id]){
            if(flagCount > 0) {
                flagCount--
                flags.setText(flagCount.toString())
                button.setBackgroundResource(R.drawable.block_flagged)
                isFlagged[id] = true
            }
            else                                                                                        //If all flags have been used, throw message
                Snackbar.make(gameScreen, "All flags have been used", Snackbar.LENGTH_LONG).show()
        }
        else{
            flagCount++
            flags.setText(flagCount.toString())
            button.setBackgroundResource(R.drawable.block_closed)
            isFlagged[id] = false
        }
    }

    fun playMinesweeper(){
        placeMines(rows*columns)
        storeEdges()
        setupBoard()

        val restart = findViewById<Button>(R.id.restart)
        restart.setOnClickListener{                                             //onClickListener for Restart button
           this.recreate()
        }
    }
}