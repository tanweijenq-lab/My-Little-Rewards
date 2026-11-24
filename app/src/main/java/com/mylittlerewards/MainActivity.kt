
package com.mylittlerewards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

private val translations = mapOf(
    "zh" to mapOf(
        "title" to "My Little Rewards",
        "parent" to "家长版",
        "child" to "孩子版",
        "coins" to "金币",
        "code" to "家庭配对码",
        "noFamily" to "尚未加入家庭",
        "create" to "创建家庭",
        "join" to "加入家庭",
        "enterCode" to "请输入家长提供的配对码",
        "tasks" to "任务",
        "gifts" to "礼物",
        "wallet" to "钱包",
        "newTask" to "新增任务",
        "taskName" to "任务名称",
        "taskCoins" to "金币奖励",
        "daily" to "每日任务",
        "addTask" to "添加任务",
        "submit" to "提交完成",
        "waitingParent" to "等待家长批准",
        "waitingChild" to "等待孩子完成",
        "approved" to "已批准",
        "approveTask" to "批准并发放金币",
        "delete" to "删除任务",
        "newGift" to "新增礼物",
        "giftName" to "礼物名称",
        "giftPrice" to "金币价格",
        "addGift" to "添加礼物",
        "requestGift" to "申请兑换",
        "approveGift" to "批准兑换",
        "redeemed" to "已兑换",
        "exam" to "考试奖励",
        "score" to "考试分数（0–100）",
        "giveReward" to "计算并发放奖励",
        "history" to "金币记录",
        "balance" to "金币余额",
        "language" to "语言",
        "connecting" to "正在连接 Firebase…",
        "noRecords" to "暂时没有记录",
        "examRule" to "90–100 分：30 金币\n80–89 分：20 金币\n70–79 分：10 金币\n低于 70 分：0 金币",
        "processing" to "正在处理…"
    ),
    "ms" to mapOf(
        "title" to "My Little Rewards",
        "parent" to "Ibu Bapa",
        "child" to "Anak",
        "coins" to "Syiling",
        "code" to "Kod keluarga",
        "noFamily" to "Belum menyertai keluarga",
        "create" to "Cipta keluarga",
        "join" to "Sertai keluarga",
        "enterCode" to "Masukkan kod daripada ibu bapa",
        "tasks" to "Tugasan",
        "gifts" to "Hadiah",
        "wallet" to "Dompet",
        "newTask" to "Tambah tugasan",
        "taskName" to "Nama tugasan",
        "taskCoins" to "Ganjaran syiling",
        "daily" to "Tugasan harian",
        "addTask" to "Tambah tugasan",
        "submit" to "Hantar selesai",
        "waitingParent" to "Menunggu kelulusan ibu bapa",
        "waitingChild" to "Menunggu anak",
        "approved" to "Diluluskan",
        "approveTask" to "Luluskan dan beri syiling",
        "delete" to "Padam tugasan",
        "newGift" to "Tambah hadiah",
        "giftName" to "Nama hadiah",
        "giftPrice" to "Harga syiling",
        "addGift" to "Tambah hadiah",
        "requestGift" to "Mohon tebus",
        "approveGift" to "Luluskan penebusan",
        "redeemed" to "Telah ditebus",
        "exam" to "Ganjaran peperiksaan",
        "score" to "Markah peperiksaan (0–100)",
        "giveReward" to "Kira dan beri ganjaran",
        "history" to "Rekod syiling",
        "balance" to "Baki syiling",
        "language" to "Bahasa",
        "connecting" to "Menyambung ke Firebase…",
        "noRecords" to "Tiada rekod lagi",
        "examRule" to "90–100: 30 syiling\n80–89: 20 syiling\n70–79: 10 syiling\nBawah 70: 0 syiling",
        "processing" to "Sedang diproses…"
    ),
    "en" to mapOf(
        "title" to "My Little Rewards",
        "parent" to "Parent",
        "child" to "Child",
        "coins" to "Coins",
        "code" to "Family code",
        "noFamily" to "Not connected to a family",
        "create" to "Create family",
        "join" to "Join family",
        "enterCode" to "Enter the code from your parent",
        "tasks" to "Tasks",
        "gifts" to "Gifts",
        "wallet" to "Wallet",
        "newTask" to "New task",
        "taskName" to "Task name",
        "taskCoins" to "Coin reward",
        "daily" to "Daily task",
        "addTask" to "Add task",
        "submit" to "Submit completed",
        "waitingParent" to "Waiting for parent approval",
        "waitingChild" to "Waiting for child",
        "approved" to "Approved",
        "approveTask" to "Approve and give coins",
        "delete" to "Delete task",
        "newGift" to "New gift",
        "giftName" to "Gift name",
        "giftPrice" to "Coin price",
        "addGift" to "Add gift",
        "requestGift" to "Request gift",
        "approveGift" to "Approve redemption",
        "redeemed" to "Redeemed",
        "exam" to "Exam reward",
        "score" to "Exam score (0–100)",
        "giveReward" to "Calculate and give reward",
        "history" to "Coin history",
        "balance" to "Coin balance",
        "language" to "Language",
        "connecting" to "Connecting to Firebase…",
        "noRecords" to "No records yet",
        "examRule" to "90–100: 30 coins\n80–89: 20 coins\n70–79: 10 coins\nBelow 70: 0 coins",
        "processing" to "Processing…"
    ),
    "vi" to mapOf(
        "title" to "My Little Rewards",
        "parent" to "Phụ huynh",
        "child" to "Trẻ em",
        "coins" to "Xu",
        "code" to "Mã gia đình",
        "noFamily" to "Chưa tham gia gia đình",
        "create" to "Tạo gia đình",
        "join" to "Tham gia",
        "enterCode" to "Nhập mã do phụ huynh cung cấp",
        "tasks" to "Nhiệm vụ",
        "gifts" to "Quà tặng",
        "wallet" to "Ví xu",
        "newTask" to "Thêm nhiệm vụ",
        "taskName" to "Tên nhiệm vụ",
        "taskCoins" to "Số xu thưởng",
        "daily" to "Nhiệm vụ hằng ngày",
        "addTask" to "Thêm nhiệm vụ",
        "submit" to "Gửi hoàn thành",
        "waitingParent" to "Đang chờ phụ huynh duyệt",
        "waitingChild" to "Đang chờ trẻ hoàn thành",
        "approved" to "Đã duyệt",
        "approveTask" to "Duyệt và thưởng xu",
        "delete" to "Xóa nhiệm vụ",
        "newGift" to "Thêm quà",
        "giftName" to "Tên quà",
        "giftPrice" to "Giá xu",
        "addGift" to "Thêm quà",
        "requestGift" to "Yêu cầu đổi quà",
        "approveGift" to "Duyệt đổi quà",
        "redeemed" to "Đã đổi",
        "exam" to "Thưởng thi cử",
        "score" to "Điểm thi (0–100)",
        "giveReward" to "Tính và trao thưởng",
        "history" to "Lịch sử xu",
        "balance" to "Số dư xu",
        "language" to "Ngôn ngữ",
        "connecting" to "Đang kết nối Firebase…",
        "noRecords" to "Chưa có lịch sử",
        "examRule" to "90–100 điểm: 30 xu\n80–89 điểm: 20 xu\n70–79 điểm: 10 xu\nDưới 70: 0 xu",
        "processing" to "Đang xử lý…"
    )
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
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(
            "my_little_rewards",
            android.content.Context.MODE_PRIVATE
        )
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var uid by remember { mutableStateOf("") }
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

    var language by remember {
        mutableStateOf(prefs.getString("language", "zh") ?: "zh")
    }
    var selectedTab by remember { mutableStateOf("tasks") }
    var message by remember { mutableStateOf("正在连接 Firebase…") }
    var loading by remember { mutableStateOf(false) }

    var familyListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var taskListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var giftListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var recordListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    fun tr(key: String): String =
        translations[language]?.get(key)
            ?: translations["zh"]?.get(key)
            ?: key

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
            } else if (snapshot != null && snapshot.exists()) {
                coins = (snapshot.getLong("coins") ?: 0L).toInt()
            }
        }

        taskListener = family.collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "任务读取失败：${error.message}"
                } else {
                    tasks = snapshot?.documents?.map { doc ->
                        TaskItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            coins = (doc.getLong("coins") ?: 0L).toInt(),
                            daily = doc.getBoolean("daily") ?: false,
                            submitted = doc.getBoolean("submitted") ?: false,
                            approved = doc.getBoolean("approved") ?: false
                        )
                    } ?: emptyList()
                }
            }

        giftListener = family.collection("gifts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "礼物读取失败：${error.message}"
                } else {
                    gifts = snapshot?.documents?.map { doc ->
                        GiftItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = (doc.getLong("price") ?: 0L).toInt(),
                            requested = doc.getBoolean("requested") ?: false,
                            approved = doc.getBoolean("approved") ?: false
                        )
                    } ?: emptyList()
                }
            }

        recordListener = family.collection("records")
            .orderBy(
                "createdAt",
                com.google.firebase.firestore.Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "记录读取失败：${error.message}"
                } else {
                    records = snapshot?.documents?.map { doc ->
                        RecordItem(
                            description = doc.getString("description") ?: "",
                            amount = (doc.getLong("amount") ?: 0L).toInt(),
                            date = doc.getString("date") ?: ""
                        )
                    } ?: emptyList()
                }
            }

        message = "${tr("connected")}：$code"
    }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            uid = user.uid
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
        val code = (1..10).map {
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
                message = "${tr("create")}成功！配对码：$code"
            }
            .addOnFailureListener {
                loading = false
                message = "创建失败：${it.message}"
            }
    }

    fun joinFamily() {
        if (uid.isBlank() || inputCode.isBlank()) return
        loading = true

        val code = inputCode.trim().uppercase(Locale.ROOT)
        val family = db.collection("families").document(code)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(family)
            if (!snapshot.exists()) {
                throw Exception("找不到家庭，请检查配对码")
            }

            val childUid = snapshot.getString("childUid") ?: ""
            if (childUid.isNotBlank() && childUid != uid) {
                throw Exception("这个家庭已经绑定其他孩子账号")
            }

            transaction.update(family, "childUid", uid)
            null
        }.addOnSuccessListener {
            loading = false
            connectFamily(code)
            message = tr("join") + "成功"
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

        db.collection("families").document(familyCode)
            .collection("tasks")
            .add(
                mapOf(
                    "name" to taskName.trim(),
                    "coins" to reward,
                    "daily" to daily,
                    "submitted" to false,
                    "approved" to false
                )
            )
            .addOnSuccessListener {
                taskName = ""
                taskCoins = "5"
                message = "任务已添加"
            }
            .addOnFailureListener {
                message = "添加失败：${it.message}"
            }
    }

    fun submitTask(task: TaskItem) {
        if (task.submitted || task.approved) return

        db.collection("families").document(familyCode)
            .collection("tasks").document(task.id)
            .update("submitted", true)
            .addOnSuccessListener {
                message = "已提交，等待家长批准"
            }
            .addOnFailureListener {
                message = "提交失败：${it.message}"
            }
    }

    fun approveTask(task: TaskItem) {
        val family = db.collection("families").document(familyCode)
        val taskRef = family.collection("tasks").document(task.id)
        val recordRef = family.collection("records").document()

        db.runTransaction { transaction ->
            val taskSnapshot = transaction.get(taskRef)
            transaction.get(family)

            if (taskSnapshot.getBoolean("approved") == true) {
                throw Exception("这个任务已经批准")
            }
            if (taskSnapshot.getBoolean("submitted") != true) {
                throw Exception("孩子还没有提交任务")
            }

            val actualCoins =
                (taskSnapshot.getLong("coins") ?: 0L).toInt()
            val actualName =
                taskSnapshot.getString("name") ?: task.name

            transaction.update(taskRef, "approved", true)
            transaction.update(
                family,
                "coins",
                FieldValue.increment(actualCoins.toLong())
            )
            transaction.set(
                recordRef,
                mapOf(
                    "description" to "任务奖励：$actualName",
                    "amount" to actualCoins,
                    "date" to SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
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
            }
            .addOnFailureListener {
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
            .add(
                mapOf(
                    "name" to giftName.trim(),
                    "price" to price,
                    "requested" to false,
                    "approved" to false
                )
            )
            .addOnSuccessListener {
                giftName = ""
                giftPrice = "20"
                message = "礼物已添加"
            }
            .addOnFailureListener {
                message = "添加失败：${it.message}"
            }
    }

    fun requestGift(gift: GiftItem) {
        if (gift.requested || gift.approved) return

        db.collection("families").document(familyCode)
            .collection("gifts").document(gift.id)
            .update("requested", true)
            .addOnSuccessListener {
                message = "兑换申请已提交"
            }
            .addOnFailureListener {
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

            val actualPrice =
                (giftSnapshot.getLong("price") ?: 0L).toInt()
            val actualName =
                giftSnapshot.getString("name") ?: gift.name

            if (balance < actualPrice) {
                throw Exception("金币不足，无法兑换")
            }

            transaction.update(giftRef, "approved", true)
            transaction.update(
                family,
                "coins",
                FieldValue.increment(-actualPrice.toLong())
            )
            transaction.set(
                recordRef,
                mapOf(
                    "description" to "兑换礼物：$actualName",
                    "amount" to -actualPrice,
                    "date" to SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
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
            transaction.update(
                family,
                "coins",
                FieldValue.increment(reward.toLong())
            )
            transaction.set(
                recordRef,
                mapOf(
                    "description" to "考试奖励：$score 分",
                    "amount" to reward,
                    "date" to SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "☁️ ${tr("title")}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink
                )

                Text(
                    if (isParent) "👨‍👩‍👧 ${tr("parent")}"
                    else "🐶 ${tr("child")}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${tr("language")}：", color = Ink)
                    listOf(
                        "zh" to "中文",
                        "ms" to "BM",
                        "en" to "English",
                        "vi" to "Tiếng Việt"
                    ).forEach { option ->
                        FilterChip(
                            selected = language == option.first,
                            onClick = {
                                language = option.first
                                prefs.edit()
                                    .putString("language", language)
                                    .apply()
                            },
                            label = { Text(option.second) }
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "🪙 $coins ${tr("coins")}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink
                        )
                        Text(
                            "${tr("code")}：${
                                familyCode.ifBlank { tr("noFamily") }
                            }",
                            color = Ink
                        )
                    }
                }

                if (uid.isBlank()) {
                    CircularProgressIndicator()
                    Text(tr("connecting"), color = Ink)
                } else if (familyCode.isBlank()) {
                    if (isParent) {
                        Text(
                            "创建家庭后，把配对码交给孩子。",
                            color = Ink
                        )
                        Button(
                            onClick = { createFamily() },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (loading) tr("processing")
                                else tr("create")
                            )
                        }
                    } else {
                        Text(tr("enterCode"), color = Ink)
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it.uppercase(Locale.ROOT)
                            },
                            label = { Text(tr("code")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { joinFamily() },
                            enabled = !loading && inputCode.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (loading) tr("processing")
                                else tr("join")
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "tasks" to tr("tasks"),
                            "gifts" to tr("gifts"),
                            "wallet" to tr("wallet")
                        ).forEach { tab ->
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
                                Text(
                                    tr("newTask"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                OutlinedTextField(
                                    value = taskName,
                                    onValueChange = { taskName = it },
                                    label = { Text(tr("taskName")) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = taskCoins,
                                    onValueChange = {
                                        taskCoins = it.filter(Char::isDigit)
                                    },
                                    label = { Text(tr("taskCoins")) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = daily,
                                        onCheckedChange = { daily = it }
                                    )
                                    Text(tr("daily"), color = Ink)
                                }
                                Button(
                                    onClick = { addTask() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(tr("addTask"))
                                }
                            }

                            tasks.forEach { task ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        Modifier.padding(12.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            (if (task.daily) "📅 " else "") +
                                                task.name,
                                            color = Ink,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("🪙 ${task.coins}", color = Ink)

                                        when {
                                            task.approved ->
                                                Text(tr("approved"), color = Ink)

                                            task.submitted && isParent ->
                                                Button(
                                                    onClick = {
                                                        approveTask(task)
                                                    }
                                                ) {
                                                    Text(tr("approveTask"))
                                                }

                                            task.submitted ->
                                                Text(tr("waitingParent"), color = Ink)

                                            !isParent ->
                                                Button(
                                                    onClick = {
                                                        submitTask(task)
                                                    }
                                                ) {
                                                    Text(tr("submit"))
                                                }

                                            else ->
                                                Text(tr("waitingChild"), color = Ink)
                                        }

                                        if (isParent) {
                                            TextButton(
                                                onClick = {
                                                    deleteTask(task)
                                                }
                                            ) {
                                                Text(tr("delete"))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "gifts" -> {
                            if (isParent) {
                                Text(
                                    tr("newGift"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                OutlinedTextField(
                                    value = giftName,
                                    onValueChange = { giftName = it },
                                    label = { Text(tr("giftName")) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = giftPrice,
                                    onValueChange = {
                                        giftPrice = it.filter(Char::isDigit)
                                    },
                                    label = { Text(tr("giftPrice")) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { addGift() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(tr("addGift"))
                                }
                            }

                            gifts.forEach { gift ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        Modifier.padding(12.dp),
                                        verticalArrangement =
                                            Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            "🎁 ${gift.name}",
                                            color = Ink,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text("🪙 ${gift.price}", color = Ink)

                                        when {
                                            gift.approved ->
                                                Text(tr("redeemed"), color = Ink)

                                            gift.requested && isParent ->
                                                Button(
                                                    onClick = {
                                                        approveGift(gift)
                                                    }
                                                ) {
                                                    Text(tr("approveGift"))
                                                }

                                            gift.requested ->
                                                Text(tr("waitingParent"), color = Ink)

                                            !isParent ->
                                                Button(
                                                    onClick = {
                                                        requestGift(gift)
                                                    }
                                                ) {
                                                    Text(tr("requestGift"))
                                                }

                                            else ->
                                                Text(tr("waitingChild"), color = Ink)
                                        }
                                    }
                                }
                            }
                        }

                        "wallet" -> {
                            Text(
                                "${tr("balance")}：$coins",
                                color = Ink,
                                style = MaterialTheme.typography.headlineSmall
                            )

                            if (isParent) {
                                Text(
                                    tr("exam"),
                                    color = Ink,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    tr("examRule"),
                                    color = Ink
                                )
                                OutlinedTextField(
                                    value = examScore,
                                    onValueChange = {
                                        examScore = it.filter(Char::isDigit)
                                    },
                                    label = { Text(tr("score")) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { addExamReward() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(tr("giveReward"))
                                }
                            }

                            Text(
                                tr("history"),
                                color = Ink,
                                style = MaterialTheme.typography.titleLarge
                            )

                            if (records.isEmpty()) {
                                Text(tr("noRecords"), color = Ink)
                            }

                            records.forEach { record ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            record.description,
                                            color = Ink
                                        )
                                    },
                                    supportingContent = {
                                        Text(record.date, color = Ink)
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
                    }
                }

                Text(message, color = Ink)
            }
        }
    }
}
