package com.pocket_plan.j7_003.data.notelist

import androidx.core.content.ContextCompat
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R

enum class NoteColors(val resolved: Int) {
    RED(ContextCompat.getColor(MainActivity.act, R.color.colorNoteRed)),
    YELLOW(ContextCompat.getColor(MainActivity.act, R.color.colorNoteYellow)),
    GREEN(ContextCompat.getColor(MainActivity.act, R.color.colorNoteGreen)),
    BLUE(ContextCompat.getColor(MainActivity.act, R.color.colorNoteBlue)),
    PURPLE(ContextCompat.getColor(MainActivity.act, R.color.colorNotePurple))
}