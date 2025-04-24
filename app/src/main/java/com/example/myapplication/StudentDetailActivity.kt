package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StudentDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        // Get the student data from the intent
        val student = intent.getParcelableExtra<Estudiante>("student")
        if (student != null) {
            // Update the UI with student details
            findViewById<TextView>(R.id.tvDetailName).text = student.name
            findViewById<TextView>(R.id.tvDetailHeight).text = "Height: ${student.height} cm"
            findViewById<TextView>(R.id.tvDetailGender).text = "Gender: ${student.gender}"
            findViewById<TextView>(R.id.tvDetailHairColor).text = "Hair Color: ${student.hairColor}"
            findViewById<TextView>(R.id.tvDetailBirthYear).text = "Birth Year: ${student.id}"
        }
    }
} 