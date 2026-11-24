
package com.mylittlerewards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.SecureRandom

private val Sky = Color(0xFF83D1F7)
private val Pale = Color(0xFFEAF8FF)
private val Ink = Color(0xFF24465C)

private data class TaskItem(
    val id: String,
    val name: String,
    val coins: Int,
    val daily: Boolean,
    val submitted: Boolean,
    val approved: Boolean
)

private data class GiftItem(
    val id: String,
    val name: String,
    val price: Int,
    val requested: Boolean,
    val approved: Boolean
)

private data class RecordItem(
    val description: String,
    val amount: Int,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isParent = applicationContext.packageName.endsWith(".parent")

        setContent {
            RewardsApp(isParent)
        }
    }
}

@Composable
private fun RewardsApp(isParent: Boolean) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var uid by remember { mutableStateOf(auth.currentUser?.uid ?: "") }
    var familyCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var coins by remember { mutableIntStateOf(0) }

    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var gifts by remember { mutableStateOf(emptyList<GiftItem>()) }
    var records by remember { mutableStateOf(emptyList<RecordItem>()) }

    var taskName by remember { mutableStateOf("") }
    var taskCoins by remember { mutableStateOf("5") }
    var daily by remember { mutableStateOf(true) }

    var giftName by remember { mutableStateOf("") }
    var giftPrice by remember { mutableStateOf("20") }

    var examScore by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("正在连接 Firebase…") }
    var loading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("tasks") }

    var familyListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var taskListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var giftListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var recordListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    val prefs = remember {
        android.app.Application().getSharedPreferences(
            "my_little_rewards",
            android.content.Context.MODE_PRIVATE
        )
    }

    fun stopListeners() {
        familyListener?.remove()
        taskListener?.remove()
        giftListener?.remove()
        recordListener?.remove()
        familyListener = null
        taskListener = null
        giftListener = null
        recordListener = null
    }

    fun connectFamily(code: String) {
        stopListeners()
        familyCode = code
        prefs.edit().putString("familyCode", code).apply()

        val family = db.collection("families").document(code)

        familyListener = family.addSnapshotListener { snapshot, error ->
            if (error != null) {
                message = "家庭资料读取失败：${error.message}"
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                coins = (snapshot.getLong("coins") ?: 0L).toInt()
            }
        }

        taskListener = family.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "任务读取失败：${error.message}"
                    return@addSnapshotListener
                }
                tasks = snapshot?.documents?.map { doc ->
                    TaskItem(
                        doc.id,
                        doc.getString("name") ?: "",
                        (doc.getLong("coins") ?: 0L).toInt(),
                        doc.getBoolean("daily") ?: false,
                        doc.getBoolean("submitted") ?: false,
                        doc.getBoolean("approved") ?: false
                    )
                } ?: emptyList()
            }

        giftListener = family.collection("gifts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "礼物读取失败：${error.message}"
                    return@addSnapshotListener
                }
                gifts = snapshot?.documents?.map { doc ->
                    GiftItem(
                        doc.id,
                        doc.getString("name") ?: "",
                        (doc.getLong("price") ?: 0L).toInt(),
                        doc.getBoolean("requested") ?: false,
                        doc.getBoolean("approved") ?: false
                    )
                } ?: emptyList()
            }

        recordListener = family.collection("records")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "记录读取失败：${error.message}"
                    return@addSnapshotListener
                }
                records = snapshot?.documents?.map { doc ->
                    RecordItem(
                        doc.getString("description") ?: "",
                        (doc.getLong("amount") ?: 0L).toInt(),
                        doc.getString("date") ?: ""
                    )
                } ?: emptyList()
            }

        message = "已连接家庭：$code"
    }

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    uid = it.user?.uid ?: ""
                    message = "登录成功"
                }
                .addOnFailureListener {
                    message = "登录失败：${it.message}"
                }
        }
    }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            val savedCode = prefs.getString("familyCode", "") ?: ""
            if (savedCode.isNotBlank()) {
                connectFamily(savedCode)
            } else {
                message = if (isParent) {
                    "请创建家庭，取得孩子配对码"
                } else {
                    "请向家长索取家庭配对码"
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopListeners() }
    }

    fun createFamily() {
        if (uid.isBlank()) return

        loading = true
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        val code = (1..8).map {
            chars[random.nextInt(chars.length)]
        }.joinToString("")

        val data = hashMapOf<String, Any>(
            "ownerUid" to uid,
            "childUid" to "",
            "coins" to 0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("families").document(code)
            .set(data)
            .addOnSuccessListener {
                loading = false
                connectFamily(code)
                message = "家庭创建成功！请把配对码 $code 交给孩子"
            }
            .addOnFailureListener {
                loading = false
                message = "创建失败：${it.message}"
            }
    }

    fun joinFamily() {
        if (uid.isBlank() || inputCode.isBlank()) return

        loading = true
        val code = inputCode.trim().uppercase()
        val family = db.collection("families").document(code)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(family)
            if (!snapshot.exists()) {
                throw Exception("找不到这个家庭，请检查配对码")
            }

            val childUid = snapshot.getString("childUid") ?: ""
            if (childUid.isNotBlank() && childUid != uid) {
                throw Exception("这个家庭已经绑定了孩子手机")
            }

            transaction.update(family, "childUid", uid)
            null
        }.addOnSuccessListener {
            loading = false
            connectFamily(code)
            message = "成功加入家庭！"
        }.addOnFailureListener {
            loading = false
            message = "加入失败：${it.message}"
        }
    }

    fun addTask() {
        val reward = taskCoins.toIntOrNull()
        if (taskName.isBlank() || reward == null || reward <= 0) {
            message = "请填写正确的任务名称和金币"
            return
        }

        val data = hashMapOf<String, Any>(
            "name" to taskName.trim(),
            "coins" to reward,
            "daily" to daily,
            "submitted" to false,
            "approved" to false
        )

        db.collection("families").document(familyCode)
            .collection("tasks").add(data)
            .addOnSuccessListener {
                taskName = ""
                taskCoins = "5"
                message = "任务已添加"
            }.addOnFailureListener {
                message = "添加失败：${it.message}"
            }
    }

    fun submitTask(task: TaskItem) {
        db.collection("families").document(familyCode)
            .collection("tasks").document(task.id)
            .update("submitted", true)
            .addOnSuccessListener {
                message = "已提交，等待家长批准"
            }.addOnFailureListener {
                message = "提交失败：${it.message}"
            }
    }

    fun approveTask(task: TaskItem) {
        val family = db.collection("families").document(familyCode)
        val taskRef = family.collection("tasks").document(task.id)
        val recordRef = family.collection("records").document()

        db.runTransaction { transaction ->
            val taskSnapshot = transaction.get(taskRef)
            val familySnapshot = transaction.get(family)

            if (taskSnapshot.getBoolean("approved") == true) {
                throw Exception("这个任务已经批准")
            }
            if (taskSnapshot.getBoolean("submitted") != true) {
                throw Exception("孩子还没有提交任务")
            }

            transaction.update(taskRef, "approved", true)
            transaction.update(family, "coins", FieldValue.increment(task.coins.toLong()))
            transaction.set(recordRef, mapOf(
                "description" to "任务奖励：${task.name}",
                "amount" to task.coins,
                "date" to java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date()),
                "createdAt" to FieldValue.serverTimestamp()
            ))
            null
        }.addOnSuccessListener {
            message = "已批准，金币已发放"
        }.addOnFailureListener {
            message = "批准失败：${it.message}"
        }
    }

    fun deleteTask(task: TaskItem) {
        db.collection("families").document(familyCode)
            .collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                message = "任务已删除"
            }.addOnFailureListener {
                message = "删除失败：${it.message}"
            }
    }

    fun addGift() {
        val price = giftPrice.toIntOrNull()
        if (giftName.isBlank() || price == null || price <= 0) {
            message = "请填写正确的礼物名称和金币价格"
            return
        }

        db.collection("families").document(familyCode)
            .collection("gifts")
            .add(mapOf(
                "name" to giftName.trim(),
                "price" to price,
                "requested" to false,
                "approved" to false
            ))
            .addOnSuccessListener {
                giftName = ""
                giftPrice = "20"
                message = "礼物已添加"
            }.addOnFailureListener {
                message = "添加失败：${it.message}"
            }
    }

    fun requestGift(gift: GiftItem) {
        db.collection("families").document(familyCode)
            .collection("gifts").document(gift.id)
            .update("requested", true)
            .addOnSuccessListener {
                message = "兑换申请已提交"
            }.addOnFailureListener {
                message = "申请失败：${it.message}"
            }
    }

    fun approveGift(gift: GiftItem) {
        val family = db.collection("families").document(familyCode)
        val giftRef = family.collection("gifts").document(gift.id)
        val recordRef = family.collection("records").document()

        db.runTransaction { transaction ->
            val giftSnapshot = transaction.get(giftRef)
            val familySnapshot = transaction.get(family)
            val balance = familySnapshot.getLong("coins") ?: 0L

            if (giftSnapshot.getBoolean("approved") == true) {
                throw Exception("这个礼物已经兑换")
            }
            if (giftSnapshot.getBoolean("requested") != true) {
                throw Exception("孩子还没有申请兑换")
            }
            if (balance < gift.price) {
                throw Exception("金币不足，无法兑换")
            }

            transaction.update(giftRef, "approved", true)
            transaction.update(family, "coins", FieldValue.increment(-gift.price.toLong()))
            transaction.set(recordRef, mapOf(
                "description" to "兑换礼物：${gift.name}",
                "amount" to -gift.price,
                "date" to java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date()),
                "createdAt" to FieldValue.serverTimestamp()
            ))
            null
        }.addOnSuccessListener {
            message = "礼物兑换已批准"
        }.addOnFailureListener {
            message = "兑换失败：${it.message}"
        }
    }

    fun addExamReward() {
        val score = examScore.toIntOrNull()
        if (score == null || score !in 0..100) {
            message = "请输入 0 至 100 的分数"
            return
        }

        val reward = when {
            score >= 90 -> 30
            score >= 80 -> 20
            score >= 70 -> 10
            else -> 0
        }

        val family = db.collection("families").document(familyCode)
        val recordRef = family.collection("records").document()

        db.runTransaction { transaction ->
            transaction.get(family)
            transaction.update(family, "coins", FieldValue.increment(reward.toLong()))
            transaction.set(recordRef, mapOf(
                "description" to "考试奖励：$score 分",
                "amount" to reward,
                "date" to java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date()),
                "createdAt" to FieldValue.serverTimestamp()
            ))
            null
        }.addOnSuccessListener {
            examScore = ""
            message = "考试奖励已发放：$reward 金币"
        }.addOnFailureListener {
            message = "发放失败：${it.message}"
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Sky,
            secondary = Ink,
            background = Pale
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Pale
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "☁️ My Little Rewards",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink
                )

                Text(
                    if (isParent) "👨‍👩‍👧 家长版" else "🐶 孩子版",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🪙 $coins 金币", style = MaterialTheme.typography.headlineMedium, color = Ink)
                        Text("家庭配对码：${familyCode.ifBlank { "尚未加入家庭" }}", color = Ink)
                    }
                }

                if (uid.isBlank()) {
                    CircularProgressIndicator()
                    Text(message, color = Ink)
                } else if (familyCode.isBlank()) {
                    if (isParent) {
                        Text("创建家庭后，把配对码交给孩子。", color = Ink)
                        Button(
                            onClick = { createFamily() },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (loading) "正在创建…" else "创建家庭")
                        }
                    } else {
                        Text("请输入家长提供的家庭配对码。", color = Ink)
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = { inputCode = it.uppercase() },
                            label = { Text("家庭配对码") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { joinFamily() },
                            enabled = !loading && inputCode.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (loading) "正在加入…" else "加入家庭")
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("tasks" to "任务", "gifts" to "礼物", "wallet" to "钱包").forEach { tab ->
                            FilterChip(
                                selected = selectedTab == tab.first,
                                onClick = { selectedTab = tab.first },
                                label = { Text(tab.second) }
                            )
                        }
                    }

                    when (selectedTab) {
                        "tasks" -> {
                            if (isParent) {
                                Text("新增任务", color = Ink, style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = taskName,
                                    onValueChange = { taskName = it },
                                    label = { Text("任务名称") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = taskCoins,
                                    onValueChange = { taskCoins = it.filter(Char::isDigit) },
                                    label = { Text("金币奖励") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Checkbox(checked = daily, onCheckedChange = { daily = it })
                                    Text("每日任务", color = Ink)
                                }
                                Button(onClick = { addTask() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("添加任务")
                                }
                            }

                            tasks.forEach { task ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            (if (task.daily) "📅 " else "") + task.name,
                                            color = Ink,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("🪙 ${task.coins}", color = Ink)
                                        when {
                                            task.approved -> Text("✅ 已批准", color = Ink)
                                            task.submitted && isParent -> Button(onClick = { approveTask(task) }) {
                                                Text("批准并发放金币")
                                            }
                                            task.submitted -> Text("等待家长批准", color = Ink)
                                            !isParent -> Button(onClick = { submitTask(task) }) {
                                                Text("提交完成")
                                            }
                                            else -> Text("等待孩子完成", color = Ink)
                                        }
                                        if (isParent) {
                                            TextButton(onClick = { deleteTask(task) }) {
                                                Text("删除任务")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "gifts" -> {
                            if (isParent) {
                                Text("新增礼物", color = Ink, style = MaterialTheme.typography.titleLarge)
                                OutlinedTextField(
                                    value = giftName,
                                    onValueChange = { giftName = it },
                                    label = { Text("礼物名称") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = giftPrice,
                                    onValueChange = { giftPrice = it.filter(Char::isDigit) },
                                    label = { Text("金币价格") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = { addGift() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("添加礼物")
                                }
                            }

                            gifts.forEach { gift ->
                                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("🎁 ${gift.name}", color = Ink, style = MaterialTheme.typography.titleMedium)
                                        Text("🪙 ${gift.price}", color = Ink)
                                        when {
                                            gift.approved -> Text("✅ 已兑换", color = Ink)
                                            gift.requested && isParent -> Button(onClick = { approveGift(gift) }) {
                                                Text("批准兑换")
                                            }
                                            gift.requested -> Text("等待家长批准", color = Ink)
                                            !isParent -> Button(onClick = { requestGift(gift) }) {
                                                Text("申请兑换")
                                            }
                                            else -> Text("等待孩子申请", color = Ink)
                                        }
                                    }
                                }
                            }
                        }

                        "wallet" -> {
                            Text("金币余额：$coins", color = Ink, style = MaterialTheme.typography.headlineSmall)

                            if (isParent) {
                                Text("考试奖励", color = Ink, style = MaterialTheme.typography.titleLarge)
                                Text("90–100 分：30 金币\n80–89 分：20 金币\n70–79 分：10 金币\n低于 70 分：0 金币", color = Ink)
                                OutlinedTextField(
                                    value = examScore,
                                    onValueChange = { examScore = it.filter(Char::isDigit) },
                                    label = { Text("考试分数（0–100）") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = { addExamReward() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("计算并发放奖励")
                                }
                            }

                            Text("金币记录", color = Ink, style = MaterialTheme.typography.titleLarge)
                            records.forEach { record ->
                                ListItem(
                                    headlineContent = { Text(record.description, color = Ink) },
                                    supportingContent = { Text(record.date, color = Ink) },
                                    trailingContent = {
                                        Text(
                                            if (record.amount >= 0) "+${record.amount}" else record.amount.toString(),
                                            color = Ink
                                        )
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Text(message, color = Ink)
            }
        }
    }
}
