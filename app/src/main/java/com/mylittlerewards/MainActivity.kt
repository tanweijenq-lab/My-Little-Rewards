
package com.mylittlerewards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SkyBlue = Color(0xFF79C9F8)
private val LightBlue = Color(0xFFEAF7FF)
private val DarkBlue = Color(0xFF397EAD)
private val Gold = Color(0xFFFFD66B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RewardApp()
        }
    }
}

@Composable
fun RewardApp() {
    var selectedTab by remember { mutableStateOf("首页") }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SkyBlue,
            background = LightBlue,
            surface = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBlue)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SkyBlue)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("☁️ My Little Rewards",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("我的奖励世界 · 家长管理版",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("早安！爸爸 🩵",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Text("今天也来为女儿的努力加油吧！")
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = SkyBlue
                        ),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(22.dp)) {
                            Text("🪙 女儿目前的金币余额",
                                color = Color.White)
                            Text("0",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White)
                            Text("金币 · My Coins",
                                color = Color.White)
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("📋", "待审核任务", "0 项",
                            Modifier.weight(1f))
                        SummaryCard("🎁", "兑换申请", "0 项",
                            Modifier.weight(1f))
                    }
                }

                item {
                    Text("☁️ 管理中心",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                }

                item {
                    ManagementCard("📝", "任务管理",
                        "新增任务、修改金币、管理每日任务")
                }

                item {
                    ManagementCard("🪙", "金币管理",
                        "奖励金币、考试成绩奖励、金币记录")
                }

                item {
                    ManagementCard("🎁", "礼物商店",
                        "新增礼物、设定兑换金币价格")
                }

                item {
                    ManagementCard("✅", "审批中心",
                        "审核女儿完成的任务及礼物兑换")
                }

                item {
                    ManagementCard("🏆", "成长统计",
                        "查看任务完成情况及金币累积")
                }

                item {
                    ManagementCard("🌐", "语言设置",
                        "简体中文 · Bahasa Melayu · English · Tiếng Việt")
                }

                item {
                    Text(
                        "☁️ 每一份努力，都值得被奖励！",
                        color = DarkBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            NavigationBar(
                containerColor = Color.White
            ) {
                listOf("首页", "任务", "金币", "商店", "设置")
                    .forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Text(
                                    when (tab) {
                                        "首页" -> "🏠"
                                        "任务" -> "📋"
                                        "金币" -> "🪙"
                                        "商店" -> "🎁"
                                        else -> "⚙️"
                                    }
                                )
                            },
                            label = { Text(tab) }
                        )
                    }
            }
        }
    }
}

@Composable
fun SummaryCard(
    emoji: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 25.sp)
            Text(title, fontSize = 13.sp, color = DarkBlue)
            Text(value, fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue)
        }
    }
}

@Composable
fun ManagementCard(
    emoji: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue)
                Text(description,
                    fontSize = 12.sp,
                    color = Color.Gray)
            }
        }
    }
}
