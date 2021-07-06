package com.example.minesweeper

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.menu_main.*

var rows: Int = 0                                                   //Store the number of rows
var columns: Int = 0                                                //Store the number of columns
var mines: Int = 0                                                  //Store number of mines

open class MainMenu: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_main)

        selectDifficulty()
    }

    fun selectDifficulty(){                                         //Function for selecting difficulty and setting values of rows, columns and mines
        val button = findViewById<Button>(R.id.ok)
        val easy = findViewById<RadioButton>(R.id.easy)
        val normal = findViewById<RadioButton>(R.id.normal)
        val hard = findViewById<RadioButton>(R.id.hard)
        val custom = findViewById<RadioButton>(R.id.custom)
        val rowsEditText = findViewById<EditText>(R.id.rowsEditText)
        val columnsEditText = findViewById<EditText>(R.id.columnsEditText)
        val minesEditText = findViewById<EditText>(R.id.minesEditText)
        val intent = Intent(this, GameActivity::class.java)     //Intent for launching GameActivity
        val difficulties = findViewById<RadioGroup>(R.id.difficulties)

        difficulties.setOnCheckedChangeListener { difficulties , checkedId ->   //Listener for toggling the visibility of custom fields
            if(checkedId == R.id.custom){
                rowsEditText.setVisibility(View.VISIBLE)
                columnsEditText.setVisibility(View.VISIBLE)
                minesEditText.setVisibility(View.VISIBLE)
            }
            else{
                rowsEditText.setVisibility(View.INVISIBLE)
                columnsEditText.setVisibility(View.INVISIBLE)
                minesEditText.setVisibility(View.INVISIBLE)
            }
        }

        button.setOnClickListener{                      //Listener for launching activity via Intent
            if (easy.isChecked){
                rows = 9
                columns = 9
                mines = 10
                startActivity(intent)
            }

            else if(normal.isChecked){
                rows = 16
                columns = 16
                mines = 40
                startActivity(intent)
            }

            else if(hard.isChecked){
                rows = 24
                columns = 24
                mines = 99
                startActivity(intent)
            }

            else if(custom.isChecked){
                rows = if(rowsEditText.getText().toString() != "") rowsEditText.getText().toString().toInt() else 0
                columns = if(columnsEditText.getText().toString() != "") columnsEditText.getText().toString().toInt() else 0
                mines = if(minesEditText.getText().toString() != "") minesEditText.getText().toString().toInt() else -1

                //Min value of rows and columns is 1, while mines is 0
                //Value of rows/columns has to be atleast half of the other side for preventing display issues
                if(rows>0 && columns>0 && rows<=30 && columns<=30 && mines>=0 && mines<rows*columns && rows>=columns/2 && columns>=rows/2)
                    startActivity(intent)
                else if(rows<columns/2)
                    Snackbar.make(menuScreen, "Rows should be atleast half of columns", Snackbar.LENGTH_LONG).show()
                else if(columns<rows/2)
                    Snackbar.make(menuScreen, "Columns should be atleast half of rows", Snackbar.LENGTH_LONG).show()
                else if(mines>=rows*columns && rows != 0 && columns != 0)
                    Snackbar.make(menuScreen, "Mines cannot be greater than or equal to grid (Rows x Columns)", Snackbar.LENGTH_LONG).show()
                else
                    Snackbar.make(menuScreen, "Please provide valid inputs", Snackbar.LENGTH_LONG).show()
            }

            else                                                                                                //No option is chosen and Ok is pressed
                Snackbar.make(menuScreen, "Please choose a difficulty", Snackbar.LENGTH_LONG).show()
        }
    }
}