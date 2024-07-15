package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File

data class LocalTime(val hour: Int, val minute: Int) {
    override fun toString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

class Tasks {
    var list = mutableListOf<String>()
    var prior = ""
    var date = ""
    var time = ""
    val savedChange = "The task is changed"
    var due = ""
    fun choosePrior() {
        do {
            println("Input the task priority (C, H, N, L):")
            prior = when(readln().uppercase()) {
                "C" -> "\u001B[101m"
                "H" -> "\u001B[103m"
                "N" -> "\u001B[102m"
                "L" -> "\u001B[104m"
                else -> ""
            }
        } while (prior == "")
    }
    fun setDate() {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            try {
                val inputDate = readln().split("-").map { it.toInt() }
                val local = LocalDate(inputDate[0], inputDate[1], inputDate[2])
                date = local.toString()
                break
            } catch (e: IllegalArgumentException) {
                println("The input date is invalid")
            }
        }
    }
    fun setTime() {
        while (true) {
            println("Input the time (hh:mm):")
            try {
                val inputTime = readln().split(":").map { it.toInt() }
                val hour = inputTime[0]
                val minute = inputTime[1]
                if (hour in 0..23 && minute in 0..59) {
                    time = LocalTime(hour, minute).toString()
                    break
                } else {
                    println("The input time is invalid")
                }
            } catch (e: IllegalArgumentException) {
                println("The input time is invalid")
            }
        }
    }
    fun setDue() {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(date.toLocalDate())
        due = when {
            numberOfDays == 0 -> "\u001B[103m"
            numberOfDays > 0 -> "\u001B[102m"
            else -> "\u001B[101m"
        }
    }
    fun addTasks() {
        choosePrior()
        setDate()
        setTime()
        setDue()
        println("Input a new task (enter a blank line to end):")
        while (true) {
            var input: String = ""
            do {
                val line = readln().trim()
                input += ("$line\n" + "")
            } while (!line.isEmpty() && !line.isBlank())
            if (input.isEmpty()) {
                break
            } else if (input.isBlank()) {
                println("The task is blank")
                break
            } else {
                list.add("$date $time $prior $due\n$input")
                break
            }
        }
    }
    fun printTasks() {
        val postfix = "+----+------------+-------+---+---+--------------------------------------------+"
        val cap2 = "| N  |    Date    | Time  | P | D |                   Task                     |"
        val cap = "$postfix\n$cap2\n$postfix"
        if (list.isEmpty()) {
            println("No tasks have been input")
        } else {
            println(cap)
            for (i in list.indices) {
                val (sDate, sTime, sPrior, sDue) = list[i].substringBefore("\n").split(" ")
                val sInput = list[i].substringAfter("\n").split("\n")
                var num = if (i + 1 < 10) " ${i + 1}  " else " ${i + 1} "
                var firstChunked = sInput[0].chunked(44)
                var second = ""
                for (i in 1 until firstChunked.size) {
                    second += "|    |            |       |   |   |${firstChunked[i].padEnd(44)}|\n"
                }
                var next = ""
                for (i in 1 until sInput.size) {
                    var chunk = sInput[i].chunked(44)
                    for (a in chunk) {
                        next += "|    |            |       |   |   |${a.padEnd(44)}|\n"
                    }
                }
                val escape = "\u001B[0m"
                val whole = buildString{
                    append("|$num| $sDate | $sTime | $sPrior $escape | $sDue $escape |${sInput[0].chunked(44)[0].padEnd(44)}|\n")
                    append(second)
                    append(next)
                    append("$postfix\n")
                }.also {
                    print(it)
                }
            }
        }
    }
    fun edit() {
        loop@ while (true) {
            if (list.isEmpty()) {
                println("No tasks have been input")
                break
            } else {
                printTasks()
            }
            while (true) {
                println("Input the task number (1-${list.size}):")
                try {
                    val taskNumber: Int = readln().toInt()
                    if (taskNumber in 1..list.size) {
                        while (true) {
                            println("Input a field to edit (priority, date, time, task):")
                            val slpittedTask = list[taskNumber - 1].split(" ").toMutableList()
                            when (readln()) {
                                "priority" -> {
                                    choosePrior()
                                    slpittedTask.set(2, prior)
                                    list[taskNumber - 1] = slpittedTask.joinToString(" ")
                                    println(savedChange)
                                    break@loop
                                }
                                "date" -> {
                                    setDate()
                                    slpittedTask.set(0, date)
                                    list[taskNumber - 1] = slpittedTask.joinToString(" ")
                                    println(savedChange)
                                    break@loop
                                }
                                "time" -> {
                                    setTime()
                                    slpittedTask.set(1, time)
                                    list[taskNumber - 1] = slpittedTask.joinToString(" ")
                                    println(savedChange)
                                    break@loop
                                }
                                "task" -> {
                                    println("Input a new task (enter a blank line to end):")
                                    var newInput = ""
                                    do {
                                        val line = readln().trim()
                                        newInput += ("$line")
                                    } while (!line.isEmpty() && !line.isBlank())
                                    val newSplit = list[taskNumber - 1].split("\n").toMutableList()
                                    newSplit.set(1, newInput)
                                    list[taskNumber - 1] = newSplit.joinToString("\n")
                                    println(savedChange)
                                    break@loop
                                }
                                else -> {
                                    println("Invalid field")
                                }
                            }
                        }
                    } else {
                        println("Invalid task number")
                    }
                } catch (e: Exception) {
                    println("Invalid task number")
                }
            }
        }
    }
    fun delete() {
        loop@ while (true) {
            if (list.isEmpty()) {
                println("No tasks have been input")
                break
            } else {
                printTasks()
            }
            while (true) {
                println("Input the task number (1-${list.size}):")
                try {
                    val taskNumber: Int = readln().toInt()
                    if (taskNumber in 1..list.size) {
                        list.removeAt(taskNumber - 1)
                        println("The task is deleted")
                        break@loop
                    } else {
                        println("Invalid task number")
                    }
                } catch (e: Exception) {
                    println("Invalid task number")
                }
            }
        }
    }
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type = Types.newParameterizedType(MutableList::class.java, String::class.java)
    val taskListAdapter = moshi.adapter<MutableList<String>>(type)
    fun convertToJson() {
        val jsonlist = taskListAdapter.toJson(list)
        val jsonFile = File("tasklist.json")
        jsonFile.writeText(jsonlist)
    }
    fun convertFromJson() {
        if (File("tasklist.json").exists()) {
            val lines = File("tasklist.json").readText()
            list = taskListAdapter.fromJson(lines)?.toMutableList()!!
        }
    }
}
fun main() {
    val tasks = Tasks()
    tasks.convertFromJson()
        while (true) {
        println("Input an action (add, print, edit, delete, end):")
        val command = readln().lowercase()
        when (command) {
            "add" -> tasks.addTasks()
            "print" -> tasks.printTasks()
            "edit" -> tasks.edit()
            "delete" -> tasks.delete()
            "end" -> {
                tasks.convertToJson()
                println("Tasklist exiting!")
                break
            }
            else -> println("The input action is invalid")
        }
    }
}