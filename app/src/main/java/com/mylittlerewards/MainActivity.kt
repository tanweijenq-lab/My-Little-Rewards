
package com.mylittlerewards

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Sky = Color(0xFF83D1F7)
private val Pale = Color(0xFFEAF8FF)
private val Ink = Color(0xFF24465C)

data class RewardTask(
    val id: Long,
    val name: String,
    val coins: Int,
    val daily: Boolean,
    val submitted: Boolean = false,
    val approved: Boolean = false
)

data class RewardGift(
    val id: Long,
    val name: String,
    val price: Int,
    val requested: Boolean = false,
    val approved: Boolean = false
)

data class CoinRecord(
    val description: String,
    val amount: Int,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RewardsApp(this)
        }
    }
}

private fun saveData(
    context: Context,
    tasks: List<RewardTask>,
    gifts: List<RewardGift>,
    records: List<CoinRecord>,
    coins: Int,
    language: String
) {
    val taskArray = JSONArray()
    tasks.forEach {
        taskArray.put(
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("coins", it.coins)
                .put("daily", it.daily)
                .put("submitted", it.submitted)
                .put("approved", it.approved)
        )
    }

    val giftArray = JSONArray()
    gifts.forEach {
        giftArray.put(
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("price", it.price)
                .put("requested", it.requested)
                .put("approved", it.approved)
        )
    }

    val recordArray = JSONArray()
    records.forEach {
        recordArray.put(
            JSONObject()
                .put("description", it.description)
                .put("amount", it.amount)
                .put("date", it.date)
        )
    }

    context.getSharedPreferences(
        "my_little_rewards",
        Context.MODE_PRIVATE
    ).edit()
        .putString("tasks", taskArray.toString())
        .putString("gifts", giftArray.toString())
        .putString("records", recordArray.toString())
        .putInt("coins", coins)
        .putString("language", language)
        .apply()
}

private fun currentDate(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).format(Date())
}

private fun textFor(language: String, key: String): String {
    val translations = mapOf(
        "中文" to mapOf(
            "tasks" to "任务",
            "gifts" to "礼物商店",
            "wallet" to "钱包",
            "settings" to "设置",
            "parent" to "家长模式",
            "child" to "孩子模式",
            "balance" to "金币余额",
            "addTask" to "新增任务",
            "taskName" to "任务名称",
            "reward" to "金币奖励",
            "daily" to "每日任务",
            "add" to "添加",
            "submit" to "提交完成",
            "pending" to "等待家长批准",
            "approve" to "批准并发放金币",
            "delete" to "删除",
            "addGift" to "新增礼物",
            "giftName" to "礼物名称",
            "price" to "金币价格",
            "redeem" to "申请兑换",
            "approveGift" to "批准兑换",
            "exam" to "考试奖励",
            "score" to "考试分数（0-100）",
            "calculate" to "计算并发放奖励",
            "language" to "选择语言",
            "history" to "金币记录",
            "waiting" to "等待批准",
            "done" to "已完成",
            "insufficient" to "金币不足",
            "saved" to "资料保存在本手机"
        ),
        "BM" to mapOf(
            "tasks" to "Tugas",
            "gifts" to "Kedai Hadiah",
            "wallet" to "Dompet",
            "settings" to "Tetapan",
            "parent" to "Mod Ibu Bapa",
            "child" to "Mod Anak",
            "balance" to "Baki Syiling",
            "addTask" to "Tambah Tugas",
            "taskName" to "Nama Tugas",
            "reward" to "Ganjaran Syiling",
            "daily" to "Tugas Harian",
            "add" to "Tambah",
            "submit" to "Hantar Selesai",
            "pending" to "Menunggu Kelulusan",
            "approve" to "Lulus dan Beri Syiling",
            "delete" to "Padam",
            "addGift" to "Tambah Hadiah",
            "giftName" to "Nama Hadiah",
            "price" to "Harga Syiling",
            "redeem" to "Mohon Tebus",
            "approveGift" to "Lulus Tebusan",
            "exam" to "Ganjaran Peperiksaan",
            "score" to "Markah (0-100)",
            "calculate" to "Kira Ganjaran",
            "language" to "Pilih Bahasa",
            "history" to "Rekod Syiling",
            "waiting" to "Menunggu",
            "done" to "Selesai",
            "insufficient" to "Syiling Tidak Cukup",
            "saved" to "Data disimpan dalam telefon"
        ),
        "EN" to mapOf(
            "tasks" to "Tasks",
            "gifts" to "Gift Shop",
            "wallet" to "Wallet",
            "settings" to "Settings",
            "parent" to "Parent Mode",
            "child" to "Child Mode",
            "balance" to "Coin Balance",
            "addTask" to "Add Task",
            "taskName" to "Task Name",
            "reward" to "Coin Reward",
            "daily" to "Daily Task",
            "add" to "Add",
            "submit" to "Submit Completion",
            "pending" to "Waiting for Approval",
            "approve" to "Approve and Give Coins",
            "delete" to "Delete",
            "addGift" to "Add Gift",
            "giftName" to "Gift Name",
            "price" to "Coin Price",
            "redeem" to "Request Redemption",
            "approveGift" to "Approve Redemption",
            "exam" to "Exam Rewards",
            "score" to "Exam Score (0-100)",
            "calculate" to "Calculate Reward",
            "language" to "Choose Language",
            "history" to "Coin History",
            "waiting" to "Pending",
            "done" to "Completed",
            "insufficient" to "Not Enough Coins",
            "saved" to "Data saved on this phone"
        ),
        "VI" to mapOf(
            "tasks" to "Nhiệm vụ",
            "gifts" to "Cửa hàng quà",
            "wallet" to "Ví tiền",
            "settings" to "Cài đặt",
            "parent" to "Chế độ phụ huynh",
            "child" to "Chế độ trẻ em",
            "balance" to "Số xu",
            "addTask" to "Thêm nhiệm vụ",
            "taskName" to "Tên nhiệm vụ",
            "reward" to "Xu thưởng",
            "daily" to "Nhiệm vụ hằng ngày",
            "add" to "Thêm",
            "submit" to "Gửi hoàn thành",
            "pending" to "Đang chờ duyệt",
            "approve" to "Duyệt và cộng xu",
            "delete" to "Xóa",
            "addGift" to "Thêm quà",
            "giftName" to "Tên quà",
            "price" to "Giá xu",
            "redeem" to "Yêu cầu đổi quà",
            "approveGift" to "Duyệt đổi quà",
            "exam" to "Thưởng thi",
            "score" to "Điểm thi (0-100)",
            "calculate" to "Tính phần thưởng",
            "language" to "Chọn ngôn ngữ",
            "history" to "Lịch sử xu",
            "waiting" to "Đang chờ",
            "done" to "Hoàn thành",
            "insufficient" to "Không đủ xu",
            "saved" to "Dữ liệu lưu trên điện thoại"
        )
    )
    return translations[language]?.get(key)
        ?: translations["中文"]?.get(key)
        ?: key
}

@Composable
fun RewardsApp(context: Context) {
    val prefs = remember {
        context.getSharedPreferences(
            "my_little_rewards",
            Context.MODE_PRIVATE
        )
    }

    fun readTasks(): List<RewardTask> {
        return runCatching {
            val array = JSONArray(prefs.getString("tasks", "[]"))
            (0 until array.length()).map {
                val o = array.getJSONObject(it)
                RewardTask(
                    o.getLong("id"),
                    o.getString("name"),
                    o.getInt("coins"),
                    o.getBoolean("daily"),
                    o.getBoolean("submitted"),
                    o.getBoolean("approved")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun readGifts(): List<RewardGift> {
        return runCatching {
            val array = JSONArray(prefs.getString("gifts", "[]"))
            (0 until array.length()).map {
                val o = array.getJSONObject(it)
                RewardGift(
                    o.getLong("id"),
                    o.getString("name"),
                    o.getInt("price"),
                    o.getBoolean("requested"),
                    o.getBoolean("approved")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun readRecords(): List<CoinRecord> {
        return runCatching {
            val array = JSONArray(prefs.getString("records", "[]"))
            (0 until array.length()).map {
                val o = array.getJSONObject(it)
                CoinRecord(
                    o.getString("description"),
                    o.getInt("amount"),
                    o.getString("date")
                )
            }
        }.getOrDefault(emptyList())
    }

    val tasks = remember {
        mutableStateListOf<RewardTask>().apply {
            addAll(readTasks())
        }
    }

    val gifts = remember {
        mutableStateListOf<RewardGift>().apply {
            addAll(readGifts())
        }
    }

    val records = remember {
        mutableStateListOf<CoinRecord>().apply {
            addAll(readRecords())
        }
    }

    var coins by remember {
        mutableIntStateOf(prefs.getInt("coins", 0))
    }

    var language by remember {
        mutableStateOf(
            prefs.getString("language", "中文") ?: "中文"
        )
    }

    var parentMode by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("tasks") }

    var taskName by remember { mutableStateOf("") }
    var taskCoins by remember { mutableStateOf("5") }
    var dailyTask by remember { mutableStateOf(true) }

    var giftName by remember { mutableStateOf("") }
    var giftPrice by remember { mutableStateOf("20") }

    var examScore by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    fun save() {
        saveData(
            context,
            tasks,
            gifts,
            records,
            coins,
            language
        )
    }

    fun addRecord(description: String, amount: Int) {
        records.add(
            0,
            CoinRecord(description, amount, currentDate())
        )
        save()
    }

    fun approveTask(task: RewardTask) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index >= 0 && !tasks[index].approved) {
            tasks[index] = task.copy(approved = true)
            coins += task.coins
            addRecord("任务奖励：${task.name}", task.coins)
        }
    }

    fun approveGift(gift: RewardGift) {
        val index = gifts.indexOfFirst { it.id == gift.id }
        if (index >= 0 &&
            !gifts[index].approved &&
            coins >= gift.price
        ) {
            gifts[index] = gift.copy(approved = true)
            coins -= gift.price
            addRecord("兑换礼物：${gift.name}", -gift.price)
        } else {
            message = textFor(language, "insufficient")
        }
    }

    val labels = listOf(
        "tasks" to textFor(language, "tasks"),
        "gifts" to textFor(language, "gifts"),
        "wallet" to textFor(language, "wallet"),
        "settings" to textFor(language, "settings")
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Sky,
            secondary = Ink,
            background = Pale
        )
    ) {
        Scaffold(
            containerColor = Pale
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp)
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "☁️ My Little Rewards",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink
                        )

                        Text(
                            "我的奖励世界",
                            color = Ink
                        )

                        Text(
                            "🪙 $coins",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = textFor(
                                    language,
                                    if (parentMode) "parent" else "child"
                                ),
                                color = Ink
                            )

                            Spacer(Modifier.width(8.dp))

                            Switch(
                                checked = parentMode,
                                onCheckedChange = {
                                    parentMode = it
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    labels.forEach { item ->
                        FilterChip(
                            selected = selectedTab == item.first,
                            onClick = { selectedTab = item.first },
                            label = { Text(item.second) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    when (selectedTab) {

                        "tasks" -> {
                            if (parentMode) {
                                Text(
                                    textFor(language, "addTask"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                OutlinedTextField(
                                    value = taskName,
                                    onValueChange = { taskName = it },
                                    label = {
                                        Text(textFor(language, "taskName"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = taskCoins,
                                    onValueChange = {
                                        taskCoins = it.filter(Char::isDigit)
                                    },
                                    label = {
                                        Text(textFor(language, "reward"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = dailyTask,
                                        onCheckedChange = {
                                            dailyTask = it
                                        }
                                    )
                                    Text(
                                        textFor(language, "daily"),
                                        color = Ink
                                    )
                                }

                                Button(
                                    onClick = {
                                        val value = taskCoins.toIntOrNull()
                                        if (taskName.isNotBlank() &&
                                            value != null && value > 0
                                        ) {
                                            tasks.add(
                                                RewardTask(
                                                    System.currentTimeMillis(),
                                                    taskName.trim(),
                                                    value,
                                                    dailyTask
                                                )
                                            )
                                            taskName = ""
                                            taskCoins = "5"
                                            save()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(textFor(language, "add"))
                                }

                                Spacer(Modifier.height(12.dp))
                            }

                            Text(
                                textFor(language, "tasks"),
                                color = Ink,
                                style = MaterialTheme.typography.titleLarge
                            )

                            tasks.forEach { task ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            (if (task.daily) "📅 " else "") +
                                                task.name,
                                            color = Ink,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            "🪙 ${task.coins}",
                                            color = Ink
                                        )

                                        when {
                                            task.approved -> {
                                                Text(
                                                    "✅ ${textFor(language, "done")}",
                                                    color = Ink
                                                )
                                            }

                                            task.submitted && parentMode -> {
                                                Button(
                                                    onClick = {
                                                        approveTask(task)
                                                    }
                                                ) {
                                                    Text(
                                                        textFor(language, "approve")
                                                    )
                                                }
                                            }

                                            task.submitted -> {
                                                Text(
                                                    textFor(language, "pending"),
                                                    color = Ink
                                                )
                                            }

                                            !parentMode -> {
                                                Button(
                                                    onClick = {
                                                        val index =
                                                            tasks.indexOfFirst {
                                                                it.id == task.id
                                                            }
                                                        if (index >= 0) {
                                                            tasks[index] =
                                                                task.copy(
                                                                    submitted = true
                                                                )
                                                            save()
                                                        }
                                                    }
                                                ) {
                                                    Text(
                                                        textFor(language, "submit")
                                                    )
                                                }
                                            }
                                        }

                                        if (parentMode) {
                                            TextButton(
                                                onClick = {
                                                    tasks.removeAll {
                                                        it.id == task.id
                                                    }
                                                    save()
                                                }
                                            ) {
                                                Text(
                                                    textFor(language, "delete")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "gifts" -> {
                            if (parentMode) {
                                Text(
                                    textFor(language, "addGift"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                OutlinedTextField(
                                    value = giftName,
                                    onValueChange = { giftName = it },
                                    label = {
                                        Text(textFor(language, "giftName"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = giftPrice,
                                    onValueChange = {
                                        giftPrice = it.filter(Char::isDigit)
                                    },
                                    label = {
                                        Text(textFor(language, "price"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val value = giftPrice.toIntOrNull()
                                        if (giftName.isNotBlank() &&
                                            value != null && value > 0
                                        ) {
                                            gifts.add(
                                                RewardGift(
                                                    System.currentTimeMillis(),
                                                    giftName.trim(),
                                                    value
                                                )
                                            )
                                            giftName = ""
                                            giftPrice = "20"
                                            save()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(textFor(language, "add"))
                                }

                                Spacer(Modifier.height(12.dp))
                            }

                            gifts.forEach { gift ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            "🎁 ${gift.name}",
                                            color = Ink,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            "🪙 ${gift.price}",
                                            color = Ink
                                        )

                                        when {
                                            gift.approved -> {
                                                Text(
                                                    "✅ ${textFor(language, "done")}",
                                                    color = Ink
                                                )
                                            }

                                            gift.requested && parentMode -> {
                                                Button(
                                                    onClick = {
                                                        approveGift(gift)
                                                    }
                                                ) {
                                                    Text(
                                                        textFor(language, "approveGift")
                                                    )
                                                }
                                            }

                                            gift.requested -> {
                                                Text(
                                                    textFor(language, "waiting"),
                                                    color = Ink
                                                )
                                            }

                                            !parentMode -> {
                                                Button(
                                                    onClick = {
                                                        val index =
                                                            gifts.indexOfFirst {
                                                                it.id == gift.id
                                                            }
                                                        if (index >= 0) {
                                                            gifts[index] =
                                                                gift.copy(
                                                                    requested = true
                                                                )
                                                            save()
                                                        }
                                                    }
                                                ) {
                                                    Text(
                                                        textFor(language, "redeem")
                                                    )
                                                }
                                            }
                                        }

                                        if (parentMode) {
                                            TextButton(
                                                onClick = {
                                                    gifts.removeAll {
                                                        it.id == gift.id
                                                    }
                                                    save()
                                                }
                                            ) {
                                                Text(
                                                    textFor(language, "delete")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "wallet" -> {
                            Text(
                                "${textFor(language, "balance")}: $coins",
                                color = Ink,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            if (parentMode) {
                                Spacer(Modifier.height(12.dp))

                                Text(
                                    textFor(language, "exam"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "90–100分：30金币\n80–89分：20金币\n70–79分：10金币\n低于70分：0金币",
                                    color = Ink
                                )

                                OutlinedTextField(
                                    value = examScore,
                                    onValueChange = {
                                        examScore = it.filter(Char::isDigit)
                                    },
                                    label = {
                                        Text(textFor(language, "score"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        val score = examScore.toIntOrNull()

                                        if (score != null &&
                                            score in 0..100
                                        ) {
                                            val reward = when {
                                                score >= 90 -> 30
                                                score >= 80 -> 20
                                                score >= 70 -> 10
                                                else -> 0
                                            }

                                            coins += reward
                                            addRecord(
                                                "考试奖励：$score 分",
                                                reward
                                            )
                                            examScore = ""
                                            message =
                                                "考试奖励：$reward 金币"
                                        } else {
                                            message =
                                                "请输入 0 至 100 的分数"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        textFor(language, "calculate")
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                textFor(language, "history"),
                                color = Ink,
                                style = MaterialTheme.typography.titleLarge
                            )

                            records.forEach { record ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            record.description,
                                            color = Ink
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            record.date,
                                            color = Ink
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            if (record.amount >= 0)
                                                "+${record.amount}"
                                            else record.amount.toString(),
                                            color = Ink
                                        )
                                    }
                                )
                                HorizontalDivider()
                            }
                        }

                        "settings" -> {
                            Text(
                                textFor(language, "language"),
                                color = Ink,
                                style = MaterialTheme.typography.titleLarge
                            )

                            val languageOptions = listOf(
                                "中文" to "简体中文",
                                "BM" to "Bahasa Melayu",
                                "EN" to "English",
                                "VI" to "Tiếng Việt"
                            )

                            languageOptions.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = language == option.first,
                                        onClick = {
                                            language = option.first
                                            save()
                                        }
                                    )
                                    Text(
                                        option.second,
                                        color = Ink
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                textFor(language, "saved"),
                                color = Ink
                            )

                            Text(
                                "任务：${tasks.size}",
                                color = Ink
                            )

                            Text(
                                "礼物：${gifts.size}",
                                color = Ink
                            )

                            Text(
                                "已批准任务：${tasks.count { it.approved }}",
                                color = Ink
                            )

                            Text(
                                "已兑换礼物：${gifts.count { it.approved }}",
                                color = Ink
                            )

                            Text(
                                "目前为单手机本地版本，尚未启用云同步。",
                                color = Ink
                            )
                        }
                    }
                }

                if (message.isNotBlank()) {
                    Text(
                        message,
                        color = Ink
                    )
                    TextButton(
                        onClick = { message = "" }
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}
