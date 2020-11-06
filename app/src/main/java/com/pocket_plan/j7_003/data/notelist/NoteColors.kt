package com.pocket_plan.j7_003.data.notelist

import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R

enum class NoteColors(val resolved: Int) {
    RED(MainActivity.act.colorForAttr(R.attr.colorNoteRed)),
    YELLOW(MainActivity.act.colorForAttr(R.attr.colorNoteYellow)),
    GREEN(MainActivity.act.colorForAttr(R.attr.colorNoteGreen)),
    BLUE(MainActivity.act.colorForAttr(R.attr.colorNoteBlue)),
    PURPLE(MainActivity.act.colorForAttr(R.attr.colorNotePurple))
}