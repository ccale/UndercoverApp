package com.example.undercoverapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.edit
import com.example.undercoverapp.ui.theme.UndercoverAppTheme

const val INNOCENT = "Innocent"
const val UNDERCOVER = "Imposteur"
const val MRWHITE = "Mister White"
const val DEFAULTWORDS = "Chat, Chien\n" + "Café, Thé\n" + "Pluie, Soleil\n" + "Livre, Film\n" + "Pizza, Sushi\n" + "Mer, Montagne\n" + "Vélo, Voiture\n" + "Piano, Guitare\n" + "Été, Hiver\n" + "Téléphone, Ordinateur"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UndercoverAppTheme {
                var textFieldName by remember { mutableStateOf("") }
                var people by remember { mutableStateOf(listOf<Person>()) }
                val sharedPref = this.getSharedPreferences("words", Context.MODE_PRIVATE)
                val wordsList = if (isListUsable(sharedPref.getString("words", DEFAULTWORDS).toString())) sharedPref.getString("words", DEFAULTWORDS).toString() else DEFAULTWORDS
                // the above looks at if the list is usable, otherwise it goes back to default stuff

                val defaultWordPair = pairPicker(wordsList)
                var innocentWord by remember { mutableStateOf(defaultWordPair.first) }
                var undercoverWord by remember { mutableStateOf(defaultWordPair.second) }

                var innocentCount by remember { mutableIntStateOf(0) }
                var undercoverCount by remember { mutableIntStateOf(0) }
                var mrWhiteCount by remember { mutableIntStateOf(0) }
                var showFirstPerson by remember { mutableStateOf(false) }


                fun startGame(){

                    // fernano exists solely to modify the people list to trigger a recompose in the people LazyColumn. DO NOT REMOVE FERNANO
                    val tempPeople = people.shuffled()
                    val fernano = Person("Fernano", INNOCENT, true)
                    people = people + fernano
                    for (person in tempPeople.subList(0, innocentCount)) {
                        person.isInGame = true
                        person.role = INNOCENT
                    }
                    for (person in tempPeople.subList(innocentCount, innocentCount + undercoverCount)){
                        person.isInGame = true
                        person.role = UNDERCOVER
                    }
                    for (person in tempPeople.subList(innocentCount + undercoverCount, tempPeople.size)){
                        person.isInGame = true
                        person.role = MRWHITE
                    }
                    val wordPair = pairPicker(wordsList)
                    innocentWord = wordPair.first
                    undercoverWord = wordPair.second
                    people = people - fernano

                    showFirstPerson = true

                }

                if (showFirstPerson){
                    Dialog(onDismissRequest = { showFirstPerson = false }) {
                        Card(modifier = Modifier
                            .fillMaxSize(0.9f)){

                            val tempPeople = people.shuffled()
                            var firstPersonText = "Il n'y a que des Mr White... ???"
                            tempPeople.forEach {
                                if (it.role != MRWHITE){
                                    firstPersonText = "${it.name} commence en premier"
                                }
                            }
                            Text(text = firstPersonText)
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = textFieldName,
                            onValueChange = {
                                text -> textFieldName = text
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = Color.Black)

                        )
                        Button(onClick = {
                            if(textFieldName.isNotBlank()){
                                people = people + (Person(name = textFieldName))
                                textFieldName = "" }
                                         },
                            modifier = Modifier.weight(0.8f)
                            ) {
                            Text(text = "Ajouter")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight(0.85f)
                    ){
                        items(people){ person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){

                                var showWord by remember { mutableStateOf(false) }
                                var showEject by remember { mutableStateOf(false)}
                                val isInGame = person.isInGame
                                
                                OutlinedButton(onClick = {
                                    people = people - person
                                }) {
                                    Text(text = "X")
                                }
                                Text(
                                    text = person.name,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .weight(1f)
                                )

                                if (isInGame){
                                    Button(onClick = { showWord = true }) {
                                        Text(text = "Voir ton mot", color = Color.White)
                                    }
                                }
                                
                                if (showWord) {
                                    Dialog(onDismissRequest = { showWord = false }) {
                                        Card(modifier = Modifier.fillMaxWidth(0.9f)) {
                                            Text(
                                                text = when (person.role) {
                                                    INNOCENT -> innocentWord
                                                    UNDERCOVER -> undercoverWord
                                                    else -> "Pas de mot!" },
                                                modifier = Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .fillMaxSize(0.7f)
                                            )
                                        }
                                    }
                                }
                                Button(onClick = { showEject = true }, enabled = isInGame) {
                                    Text(text = if (isInGame) "Virer" else person.role,
                                        color = if (isInGame) Color.White else Color.Black)
                                }

                                if (showEject){
                                    person.isInGame = false
                                    Dialog(onDismissRequest = { showEject = false }) {
                                        Card(modifier = Modifier.fillMaxSize(0.9f)) {
                                            Column {
                                                Text(
                                                    text = when (person.role) {
                                                        INNOCENT -> "${person.name} était innocent!"
                                                        UNDERCOVER -> "${person.name} était undercover!"
                                                        else -> "${person.name} était mister white!"
                                                    },
                                                    modifier = Modifier
                                                        .align(Alignment.CenterHorizontally)
                                                )
                                                if (person.role == MRWHITE){
                                                    var isRightWord by remember { mutableStateOf(false) }
                                                    var isButtonBeenClicked by remember { mutableStateOf(false)}
                                                    var textField by remember { mutableStateOf("") }
                                                    Column(modifier = Modifier
                                                        .padding(8.dp)
                                                    ) {
                                                        OutlinedTextField(
                                                            value = textField,
                                                            onValueChange = { text -> textField = text},
                                                            modifier = Modifier
                                                                .fillMaxWidth(0.6f)
                                                                .align(Alignment.CenterHorizontally)
                                                        )
                                                        Button(
                                                            onClick = { if (textField.lowercase().trim() == innocentWord.lowercase().trim()){ isRightWord = true } ; isButtonBeenClicked = true },
                                                            modifier = Modifier
                                                                .fillMaxWidth(0.4f)
                                                                .align(Alignment.CenterHorizontally)
                                                        ) {
                                                            Text(text = "Deviner le mot")
                                                        }
                                                        if (isButtonBeenClicked){
                                                            var answerText = "Ce n'est pas le bon mot !"
                                                            if (isRightWord) { answerText = "${person.name} a trouvé le bon mot et a gagné !"}
                                                            Text(text = answerText,
                                                                modifier = Modifier
                                                                    .align(Alignment.CenterHorizontally)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        var showDialogAddWords by remember { mutableStateOf(false) }
                        var showDialogStartGame by remember { mutableStateOf(false) }
                        var wordsListValue by remember { mutableStateOf(wordsList) }
                        var isUsableList by remember { mutableStateOf(isListUsable(wordsListValue)) }

                        Button(onClick = { showDialogAddWords = true}) {
                            Text(text = "Ajouter des mots")
                        }
                        
                        if (showDialogAddWords){
                            Dialog(
                                onDismissRequest = { showDialogAddWords = false },
                                ) {
                                
                                Card(
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = Modifier
                                        .fillMaxSize(0.95f)
                                        .border(1.dp, color = Color.Blue)
                                ) {
                                    Column (
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .align(Alignment.CenterHorizontally)
                                    ) {
                                        OutlinedTextField(
                                            value = wordsListValue,
                                            onValueChange = { text -> wordsListValue = text
                                                            isUsableList = isListUsable(wordsListValue)
                                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .align(Alignment.CenterHorizontally)
                                                .padding(8.dp)
                                            )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.3f)
                                        ) {
                                            Button(onClick = {
                                                if (isListUsable(wordsListValue)) {
                                                    sharedPref.edit {
                                                        putString("words", wordsListValue)
                                                       apply()
                                                    }
                                                }
                                                else {
                                                    wordsListValue = wordsList
                                                }
                                                showDialogAddWords = false
                                            },
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                ) {
                                                Text(text = "Confirmer")
                                            }
                                            val message: String = if (!isUsableList) "Un saut de ligne entre chaque paire, une virgule entre chaque mots dans une paire" else "Cette liste fonctione"
                                            Text(text = message, modifier = Modifier.fillMaxSize())

                                        }
                                    }
                                }
                            }
                        }
                        
                        Button(onClick = { showDialogStartGame = true}) {
                            Text(text = "Lancer une partie!")
                        }

                        if (showDialogStartGame){
                            Dialog(onDismissRequest = { showDialogStartGame = false }) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize(0.9f)
                                ) {
                                    Column(modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)) {
                                        val maxValue = people.size
                                        var tempInnocent by remember { mutableIntStateOf(0) }
                                        var tempUndercover  by remember { mutableIntStateOf(0) }
                                        var tempMrWhite by remember { mutableIntStateOf(0) }

                                        var enoughPeople by remember { mutableStateOf(tempInnocent + tempMrWhite + tempUndercover == maxValue)}

                                        fun isEnoughPeople(a: Int, b: Int, c: Int, d:Int): Boolean{
                                            return (a + b + c == d)
                                        }

                                        Text(text = "$maxValue joueurs")
                                        NumberPicker("Innocent", maxValue ) { number -> tempInnocent = number ; enoughPeople = isEnoughPeople(tempInnocent, tempUndercover, tempMrWhite, maxValue) }
                                        NumberPicker("Imposteurs", maxValue ) { number -> tempUndercover = number ; enoughPeople = isEnoughPeople(tempInnocent, tempUndercover, tempMrWhite, maxValue) }
                                        NumberPicker("Mr White", maxValue ) { number -> tempMrWhite = number ; enoughPeople = isEnoughPeople(tempInnocent, tempUndercover, tempMrWhite, maxValue)}
                                        
                                        Spacer(modifier = Modifier.size(8.dp))
                                        
                                        Button(onClick = { innocentCount = tempInnocent ; undercoverCount = tempUndercover ; mrWhiteCount = tempMrWhite ;  showDialogStartGame = false ; startGame()},
                                            enabled = enoughPeople) {
                                            Text(text = if (enoughPeople) "Lancer la partie" else "Mauvais nombre de joueurs")
                                        }
                                    }
                                }
                            }
                        }


                    }
                }
            }
        }
    }
}

class Person(val name: String, var role: String = INNOCENT, var isInGame: Boolean = true)

@Composable
fun NumberPicker(label: String, maxValue: Int, numberPicked:(Int) -> Unit){
    var number by remember { mutableIntStateOf(0) }
    Row {
        Text(text = label, modifier = Modifier.align(Alignment.CenterVertically))
        TextButton(onClick = { if ( number > 0) { number -= 1} ; numberPicked(number) }) {
            Text(text = "<")
        }
        Text(text = number.toString(),
            modifier = Modifier
                .width(32.dp)
                .align(Alignment.CenterVertically)
            )
        TextButton(onClick = { if ( number < maxValue) { number += 1} ; numberPicked(number) }) {
            Text(text = ">")
        }
    }
}

fun pairPicker(wordsList: String): Pair<String, String> {
    var words: List<String> = mutableListOf()
    wordsList.split("\n").map { line ->
        if (line.isNotBlank()) {
            words = words + line.trim()
        }
    }
    val pairString = words[words.indices.random()]
    val random: Int = listOf(0,1).random()
    return Pair(
        pairString.split(",")[0 + random].trim(),
        pairString.split(",")[1 - random].trim()
    )
}

fun isListUsable(wordsList: String): Boolean {
    if (wordsList.isBlank()) {
        return false
    }
    val lines = wordsList.split("\n")
    lines.forEach {
        val parts = it.split(",")
        if (parts.size != 2){ return false }
        if (parts[0].isBlank() or parts[1].isBlank()){ return false }
    }
    return true
}

