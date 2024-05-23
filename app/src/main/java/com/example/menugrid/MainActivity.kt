package com.example.menugrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.menugrid.ui.theme.MenuGridTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MenuGridTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowGridMenu()
                }
            }
        }
    }
}

@Composable
fun ShowGridMenu() {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            modifier = Modifier
                .height(900.dp)
                .padding(16.dp),
            columns = GridCells.Adaptive(minSize = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menuledoff),
                    text = "Turn On LED", value = "led_on"
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusweep),
                    text = "Sweeping", value = "servo_sweeping"
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Next Feature", value = ""
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Next Feature", value = ""
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Next Feature", value = ""
                )
            }
            item {
                ChooseBoxFormat(
                    painterResource(R.drawable.menusetting),
                    text = "Next Feature", value = ""
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ChooseBoxFormat(icon: Painter, text: String, value: String) {
    Box(
        modifier = Modifier
            .wrapContentSize() // wrap content height and width
            .aspectRatio(1f) // Make the button square
            .border(
                1.dp,
                Color.Black,
                shape = RoundedCornerShape(8.dp)
            ) // Add border
            .padding(8.dp) // Add padding to adjust the height
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center the content vertically
        ) {
            Icon(
                painter = icon,
                contentDescription = "Icon",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text,
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}