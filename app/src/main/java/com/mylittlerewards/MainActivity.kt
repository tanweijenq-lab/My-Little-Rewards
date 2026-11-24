
package com.mylittlerewards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Sky = Color(0xFF8BD5F5)
private val DeepSky = Color(0xFF55B8E8)
private val PaleBlue = Color(0xFFEAF8FF)
private val CloudWhite = Color(0xFFFFFFFF)
private val Cream = Color(0xFFFFFBF0)
private val Ink = Color(0xFF34566B)
private val SoftPink = Color(0xFFFFE5EF)
private val SoftYellow = Color(0xFFFFF2C9)
private val SoftGreen = Color(0xFFE3F6E9)

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

private val words = mapOf(
    "zh" to mapOf(
        "parent" to "家长专属空间",
        "child" to "我的奖励乐园",
        "coins" to "我的金币",
        "family" to "家庭配对码",
        "create" to "创建家庭",
        "join" to "加入家庭",
        "enterCode" to "请输入家长提供的配对码",
        "tasks" to "每日任务",
        "gifts" to "梦想礼物",
        "wallet" to "金币账户",
        "newTask" to "新增任务",
        "taskName" to "任务名称",
        "taskCoins" to "完成奖励金币",
        "daily" to "每日重复任务",
        "addTask" to "添加任务",
        "submit" to "我完成了！",
        "waitingParent" to "等待家长确认",
        "waitingChild" to "等待孩子完成",
        "approved" to "已完成并获得奖励",
        "approveTask" to "确认并发放金币",
        "delete" to "删除任务",
        "newGift" to "新增梦想礼物",
        "giftName" to "礼物名称",
        "giftPrice" to "兑换所需金币",
        "addGift" to "添加礼物",
        "requestGift" to "申请兑换",
        "approveGift" to "批准兑换",
        "redeemed" to "已经兑换",
        "exam" to "考试成绩奖励",
        "score" to "考试分数（0–100）",
        "giveReward" to "发放考试奖励",
        "history" to "金币收支记录",
        "balance" to "目前金币余额",
        "language" to "语言",
        "connecting" to "正在连接 Firebase…",
        "noRecords" to "暂时还没有记录",
        "examRule" to "90–100 分：30 金币\n80–89 分：20 金币\n70–79 分：10 金币\n低于 70 分：0 金币",
        "processing" to "处理中…",
        "noFamily" to "尚未连接家庭",
        "hello" to "今天也要加油哦！",
        "parentTip" to "管理任务、奖励和孩子的金币。",
        "childTip" to "完成小任务，收集金币，兑换喜欢的礼物！",
        "noTasks" to "还没有任务，等待家长添加吧！",
        "noGifts" to "还没有礼物，等待家长添加吧！",
        "taskTitle" to "任务小天地",
        "giftTitle" to "我的梦想礼物",
        "walletTitle" to "我的金币小银行",
        "codeTip" to "请把这个配对码交给孩子的手机。",
        "insufficient" to "金币不足，暂时不能兑换。",
        "logoutFamily" to "解除本机家庭连接",
        "confirmReset" to "确定要解除本机连接吗？",
        "cancel" to "取消",
        "confirm" to "确定",
        "pending" to "等待处理",
        "dailyTag" to "每日",
        "onceTag" to "单次"
    ),
    "ms" to mapOf(
        "parent" to "Ruang Ibu Bapa",
        "child" to "Taman Ganjaran Saya",
        "coins" to "Syiling Saya",
        "family" to "Kod Keluarga",
        "create" to "Cipta Keluarga",
        "join" to "Sertai Keluarga",
        "enterCode" to "Masukkan kod daripada ibu bapa",
        "tasks" to "Tugasan",
        "gifts" to "Hadiah Impian",
        "wallet" to "Dompet Syiling",
        "newTask" to "Tambah Tugasan",
        "taskName" to "Nama Tugasan",
        "taskCoins" to "Ganjaran Syiling",
        "daily" to "Tugasan Harian",
        "addTask" to "Tambah Tugasan",
        "submit" to "Saya Siap!",
        "waitingParent" to "Menunggu pengesahan ibu bapa",
        "waitingChild" to "Menunggu anak",
        "approved" to "Selesai dan diberi ganjaran",
        "approveTask" to "Luluskan dan beri syiling",
        "delete" to "Padam",
        "newGift" to "Tambah Hadiah",
        "giftName" to "Nama Hadiah",
        "giftPrice" to "Harga Syiling",
        "addGift" to "Tambah Hadiah",
        "requestGift" to "Mohon Tebus",
        "approveGift" to "Luluskan Tebusan",
        "redeemed" to "Telah ditebus",
        "exam" to "Ganjaran Peperiksaan",
        "score" to "Markah Peperiksaan (0–100)",
        "giveReward" to "Beri Ganjaran",
        "history" to "Sejarah Syiling",
        "balance" to "Baki Syiling",
        "language" to "Bahasa",
        "connecting" to "Menyambung Firebase…",
        "noRecords" to "Tiada rekod lagi",
        "examRule" to "90–100: 30 syiling\n80–89: 20 syiling\n70–79: 10 syiling\nBawah 70: 0 syiling",
        "processing" to "Memproses…",
        "noFamily" to "Belum menyertai keluarga",
        "hello" to "Teruskan usaha hari ini!",
        "parentTip" to "Urus tugasan, hadiah dan syiling anak.",
        "childTip" to "Siapkan tugasan dan kumpul syiling!",
        "noTasks" to "Tiada tugasan lagi.",
        "noGifts" to "Tiada hadiah lagi.",
        "taskTitle" to "Tugasan Saya",
        "giftTitle" to "Hadiah Impian Saya",
        "walletTitle" to "Bank Syiling Saya",
        "codeTip" to "Berikan kod ini kepada telefon anak.",
        "insufficient" to "Syiling tidak mencukupi.",
        "logoutFamily" to "Putuskan sambungan keluarga",
        "confirmReset" to "Putuskan sambungan pada telefon ini?",
        "cancel" to "Batal",
        "confirm" to "Sahkan",
        "pending" to "Menunggu",
        "dailyTag" to "Harian",
        "onceTag" to "Sekali"
    ),
    "en" to mapOf(
        "parent" to "Parent Space",
        "child" to "My Reward Garden",
        "coins" to "My Coins",
        "family" to "Family Code",
        "create" to "Create Family",
        "join" to "Join Family",
        "enterCode" to "Enter the code from your parent",
        "tasks" to "Tasks",
        "gifts" to "Dream Gifts",
        "wallet" to "Coin Wallet",
        "newTask" to "New Task",
        "taskName" to "Task Name",
        "taskCoins" to "Coin Reward",
        "daily" to "Daily Task",
        "addTask" to "Add Task",
        "submit" to "I did it!",
        "waitingParent" to "Waiting for parent",
        "waitingChild" to "Waiting for child",
        "approved" to "Completed and rewarded",
        "approveTask" to "Approve and give coins",
        "delete" to "Delete",
        "newGift" to "New Gift",
        "giftName" to "Gift Name",
        "giftPrice" to "Coin Price",
        "addGift" to "Add Gift",
        "requestGift" to "Request Gift",
        "approveGift" to "Approve Redemption",
        "redeemed" to "Redeemed",
        "exam" to "Exam Rewards",
        "score" to "Exam Score (0–100)",
        "giveReward" to "Give Exam Reward",
        "history" to "Coin History",
        "balance" to "Current Balance",
        "language" to "Language",
        "connecting" to "Connecting to Firebase…",
        "noRecords" to "No records yet",
        "examRule" to "90–100: 30 coins\n80–89: 20 coins\n70–79: 10 coins\nBelow 70: 0 coins",
        "processing" to "Processing…",
        "noFamily" to "Not connected",
        "hello" to "Let's do our best today!",
        "parentTip" to "Manage tasks, gifts and your child's coins.",
        "childTip" to "Complete tasks, collect coins and enjoy rewards!",
        "noTasks" to "No tasks yet.",
        "noGifts" to "No gifts yet.",
        "taskTitle" to "My Tasks",
        "giftTitle" to "My Dream Gifts",
        "walletTitle" to "My Coin Bank",
        "codeTip" to "Share this code with your child's phone.",
        "insufficient" to "Not enough coins.",
        "logoutFamily" to "Disconnect this device",
        "confirmReset" to "Disconnect this device from the family?",
        "cancel" to "Cancel",
        "confirm" to "Confirm",
        "pending" to "Pending",
        "dailyTag" to "Daily",
        "onceTag" to "Once"
    ),
    "vi" to mapOf(
        "parent" to "Khu vực phụ huynh",
        "child" to "Vườn phần thưởng",
        "coins" to "Xu của tôi",
        "family" to "Mã gia đình",
        "create" to "Tạo gia đình",
        "join" to "Tham gia gia đình",
        "enterCode" to "Nhập mã từ phụ huynh",
        "tasks" to "Nhiệm vụ",
        "gifts" to "Quà mơ ước",
        "wallet" to "Ví xu",
        "newTask" to "Thêm nhiệm vụ",
        "taskName" to "Tên nhiệm vụ",
        "taskCoins" to "Số xu thưởng",
        "daily" to "Nhiệm vụ hằng ngày",
        "addTask" to "Thêm nhiệm vụ",
        "submit" to "Con đã làm xong!",
        "waitingParent" to "Chờ phụ huynh xác nhận",
        "waitingChild" to "Chờ trẻ hoàn thành",
        "approved" to "Đã hoàn thành và nhận thưởng",
        "approveTask" to "Duyệt và trao xu",
        "delete" to "Xóa",
        "newGift" to "Thêm quà",
        "giftName" to "Tên quà",
        "giftPrice" to "Giá xu",
        "addGift" to "Thêm quà",
        "requestGift" to "Yêu cầu đổi quà",
        "approveGift" to "Duyệt đổi quà",
        "redeemed" to "Đã đổi",
        "exam" to "Thưởng điểm thi",
        "score" to "Điểm thi (0–100)",
        "giveReward" to "Trao thưởng",
        "history" to "Lịch sử xu",
        "balance" to "Số dư xu",
        "language" to "Ngôn ngữ",
        "connecting" to "Đang kết nối Firebase…",
        "noRecords" to "Chưa có lịch sử",
        "examRule" to "90–100: 30 xu\n80–89: 20 xu\n70–79: 10 xu\nDưới 70: 0 xu",
        "processing" to "Đang xử lý…",
        "noFamily" to "Chưa kết nối gia đình",
        "hello" to "Hôm nay cũng cố gắng nhé!",
        "parentTip" to "Quản lý nhiệm vụ, quà và xu của trẻ.",
        "childTip" to "Hoàn thành nhiệm vụ để nhận xu!",
        "noTasks" to "Chưa có nhiệm vụ.",
        "noGifts" to "Chưa có quà.",
        "taskTitle" to "Nhiệm vụ của tôi",
        "giftTitle" to "Quà mơ ước",
        "walletTitle" to "Ngân hàng xu",
        "codeTip" to "Chia sẻ mã này với điện thoại của trẻ.",
        "insufficient" to "Không đủ xu.",
        "logoutFamily" to "Ngắt kết nối thiết bị",
        "confirmReset" to "Ngắt kết nối gia đình trên thiết bị này?",
        "cancel" to "Hủy",
        "confirm" to "Xác nhận",
        "pending" to "Đang chờ",
        "dailyTag" to "Hằng ngày",
        "onceTag" to "Một lần"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isParent = packageName.endsWith(".parent")
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
    var showDisconnectDialog by remember { mutableStateOf(false) }

    var familyListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var taskListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var giftListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var recordListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    fun tr(key: String): String =
        words[language]?.get(key) ?: words["zh"]?.get(key) ?: key

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
            } else {
                message = "找不到家庭资料，请检查配对码。"
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

        message = "已连接家庭：$code"
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
                    "请创建家庭，取得孩子配对码。"
                } else {
                    "请向家长索取家庭配对码。"
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
                message = "家庭创建成功！配对码：$code"
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
                throw Exception("找不到家庭，请检查配对码。")
            }

            val childUid = snapshot.getString("childUid") ?: ""
            if (childUid.isNotBlank() && childUid != uid) {
                throw Exception("这个家庭已经绑定其他孩子账号。")
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
            message = "请填写正确的任务名称和金币数量。"
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
                message = "任务已添加！"
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
                message = "已提交，等待家长批准！"
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
                throw Exception("这个任务已经批准。")
            }
            if (taskSnapshot.getBoolean("submitted") != true) {
                throw Exception("孩子还没有提交任务。")
            }

            val reward = taskSnapshot.getLong("coins") ?: 0L
            val name = taskSnapshot.getString("name") ?: task.name

            transaction.update(taskRef, "approved", true)
            transaction.update(
                family,
                "coins",
                FieldValue.increment(reward)
            )
            transaction.set(
                recordRef,
                mapOf(
                    "description" to "任务奖励：$name",
                    "amount" to reward.toInt(),
                    "date" to SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            null
        }.addOnSuccessListener {
            message = "任务已批准，金币已发放！"
        }.addOnFailureListener {
            message = "批准失败：${it.message}"
        }
    }

    fun deleteTask(task: TaskItem) {
        db.collection("families").document(familyCode)
            .collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                message = "任务已删除。"
            }
            .addOnFailureListener {
                message = "删除失败：${it.message}"
            }
    }

    fun addGift() {
        val price = giftPrice.toIntOrNull()
        if (giftName.isBlank() || price == null || price <= 0) {
            message = "请填写正确的礼物名称和金币价格。"
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
                message = "礼物已添加！"
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
                message = "兑换申请已提交！"
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
                throw Exception("这个礼物已经兑换。")
            }
            if (giftSnapshot.getBoolean("requested") != true) {
                throw Exception("孩子还没有申请兑换。")
            }

            val price = giftSnapshot.getLong("price") ?: 0L
            val name = giftSnapshot.getString("name") ?: gift.name

            if (balance < price) {
                throw Exception("金币不足，无法兑换。")
            }

            transaction.update(giftRef, "approved", true)
            transaction.update(
                family,
                "coins",
                FieldValue.increment(-price)
            )
            transaction.set(
                recordRef,
                mapOf(
                    "description" to "兑换礼物：$name",
                    "amount" to -price.toInt(),
                    "date" to SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                    ).format(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            null
        }.addOnSuccessListener {
            message = "礼物兑换已批准！"
        }.addOnFailureListener {
            message = "兑换失败：${it.message}"
        }
    }

    fun addExamReward() {
        val score = examScore.toIntOrNull()
        if (score == null || score !in 0..100) {
            message = "请输入 0 至 100 的考试分数。"
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
            message = "考试奖励已发放：$reward 金币！"
        }.addOnFailureListener {
            message = "发放失败：${it.message}"
        }
    }

    fun disconnectDevice() {
        stopListeners()
        prefs.edit().remove("familyCode").apply()
        familyCode = ""
        coins = 0
        tasks = emptyList()
        gifts = emptyList()
        records = emptyList()
        inputCode = ""
        message = "本机已解除家庭连接。"
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = DeepSky,
            onPrimary = Color.White,
            secondary = Ink,
            background = PaleBlue,
            surface = CloudWhite
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PaleBlue
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 顶部玉桂狗风格标题
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Sky
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            "☁️ ૮₍˶ᵔ ᵕ ᵔ˶₎ა ☁️",
                            fontSize = 27.sp,
                            color = Color.White
                        )
                        Text(
                            "MY LITTLE REWARDS",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            if (isParent) tr("parent") else tr("child"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            tr("hello"),
                            fontSize = 13.sp,
                            color = Ink
                        )
                    }
                }

                // 语言选择
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CloudWhite
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "🌐 ${tr("language")}",
                            color = Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                "zh" to "中文",
                                "ms" to "BM",
                                "en" to "EN",
                                "vi" to "VI"
                            ).forEach { option ->
                                FilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = language == option.first,
                                    onClick = {
                                        language = option.first
                                        prefs.edit()
                                            .putString("language", language)
                                            .apply()
                                    },
                                    label = {
                                        Text(
                                            option.second,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 金币余额卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SoftYellow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                tr("coins"),
                                color = Ink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "$coins",
                                color = Ink,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp
                            )
                            Text(
                                tr("balance"),
                                color = Ink,
                                fontSize = 12.sp
                            )
                        }
                        Text("🪙 ⭐", fontSize = 36.sp)
                    }
                }

                // 家庭配对区域
                if (uid.isBlank()) {
                    CuteCard {
                        CircularProgressIndicator(color = DeepSky)
                        Text(tr("connecting"), color = Ink)
                    }
                } else if (familyCode.isBlank()) {
                    CuteCard {
                        Text(
                            "☁️ ${tr("family")}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        if (isParent) {
                            Text(
                                "创建家庭后，把配对码交给孩子的手机。",
                                color = Ink
                            )
                            CuteButton(
                                text = if (loading) tr("processing")
                                else "✨ ${tr("create")}",
                                enabled = !loading,
                                onClick = { createFamily() }
                            )
                        } else {
                            Text(tr("enterCode"), color = Ink)
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = {
                                    inputCode = it.uppercase(Locale.ROOT)
                                },
                                label = { Text(tr("family")) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                            CuteButton(
                                text = if (loading) tr("processing")
                                else "💙 ${tr("join")}",
                                enabled = !loading && inputCode.isNotBlank(),
                                onClick = { joinFamily() }
                            )
                        }
                    }
                } else {
                    // 家庭配对码
                    CuteCard {
                        Text(
                            "🏡 ${tr("family")}",
                            color = Ink,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            familyCode,
                            color = DeepSky,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        if (isParent) {
                            Text(tr("codeTip"), color = Ink, fontSize = 12.sp)
                        }
                    }

                    Text(
                        if (isParent) tr("parentTip") else tr("childTip"),
                        color = Ink,
                        fontSize = 13.sp
                    )

                    // 三个主功能标签
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        CuteTab(
                            modifier = Modifier.weight(1f),
                            selected = selectedTab == "tasks",
                            text = "📋\n${tr("tasks")}"
                        ) {
                            selectedTab = "tasks"
                        }
                        CuteTab(
                            modifier = Modifier.weight(1f),
                            selected = selectedTab == "gifts",
                            text = "🎁\n${tr("gifts")}"
                        ) {
                            selectedTab = "gifts"
                        }
                        CuteTab(
                            modifier = Modifier.weight(1f),
                            selected = selectedTab == "wallet",
                            text = "🪙\n${tr("wallet")}"
                        ) {
                            selectedTab = "wallet"
                        }
                    }

                    when (selectedTab) {
                        "tasks" -> {
                            SectionTitle("☁️ ${tr("taskTitle")}")

                            if (isParent) {
                                CuteCard {
                                    Text(
                                        "➕ ${tr("newTask")}",
                                        color = Ink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    OutlinedTextField(
                                        value = taskName,
                                        onValueChange = { taskName = it },
                                        label = { Text(tr("taskName")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = taskCoins,
                                        onValueChange = {
                                            taskCoins = it.filter(Char::isDigit)
                                        },
                                        label = { Text(tr("taskCoins")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
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
                                    CuteButton(
                                        text = "＋ ${tr("addTask")}",
                                        onClick = { addTask() }
                                    )
                                }
                            }

                            if (tasks.isEmpty()) {
                                EmptyCard("☁️", tr("noTasks"))
                            }

                            tasks.forEach { task ->
                                CuteCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                task.name,
                                                color = Ink,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "🪙 ${task.coins}   ·   ${
                                                    if (task.daily) tr("dailyTag")
                                                    else tr("onceTag")
                                                }",
                                                color = DeepSky,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Text("⭐", fontSize = 25.sp)
                                    }

                                    when {
                                        task.approved -> StatusText(
                                            "✅ ${tr("approved")}",
                                            SoftGreen
                                        )
                                        task.submitted && isParent -> CuteButton(
                                            text = "💙 ${tr("approveTask")}",
                                            onClick = { approveTask(task) }
                                        )
                                        task.submitted -> StatusText(
                                            "⏳ ${tr("waitingParent")}",
                                            SoftYellow
                                        )
                                        !isParent -> CuteButton(
                                            text = "✨ ${tr("submit")}",
                                            onClick = { submitTask(task) }
                                        )
                                        else -> StatusText(
                                            "🌱 ${tr("waitingChild")}",
                                            PaleBlue
                                        )
                                    }

                                    if (isParent) {
                                        TextButton(
                                            onClick = { deleteTask(task) }
                                        ) {
                                            Text(
                                                "🗑 ${tr("delete")}",
                                                color = Color(0xFFCE718B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "gifts" -> {
                            SectionTitle("🎁 ${tr("giftTitle")}")

                            if (isParent) {
                                CuteCard {
                                    Text(
                                        "➕ ${tr("newGift")}",
                                        color = Ink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    OutlinedTextField(
                                        value = giftName,
                                        onValueChange = { giftName = it },
                                        label = { Text(tr("giftName")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = giftPrice,
                                        onValueChange = {
                                            giftPrice = it.filter(Char::isDigit)
                                        },
                                        label = { Text(tr("giftPrice")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
                                    )
                                    CuteButton(
                                        text = "＋ ${tr("addGift")}",
                                        onClick = { addGift() }
                                    )
                                }
                            }

                            if (gifts.isEmpty()) {
                                EmptyCard("🎀", tr("noGifts"))
                            }

                            gifts.forEach { gift ->
                                CuteCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎁", fontSize = 32.sp)
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                gift.name,
                                                color = Ink,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                "🪙 ${gift.price}",
                                                color = DeepSky
                                            )
                                        }
                                    }

                                    when {
                                        gift.approved -> StatusText(
                                            "🎉 ${tr("redeemed")}",
                                            SoftGreen
                                        )
                                        gift.requested && isParent -> CuteButton(
                                            text = "💙 ${tr("approveGift")}",
                                            onClick = { approveGift(gift) }
                                        )
                                        gift.requested -> StatusText(
                                            "⏳ ${tr("waitingParent")}",
                                            SoftYellow
                                        )
                                        !isParent -> CuteButton(
                                            text = "🎀 ${tr("requestGift")}",
                                            enabled = coins >= gift.price,
                                            onClick = { requestGift(gift) }
                                        )
                                        else -> StatusText(
                                            "🌱 ${tr("waitingChild")}",
                                            PaleBlue
                                        )
                                    }

                                    if (!isParent && !gift.approved &&
                                        !gift.requested && coins < gift.price
                                    ) {
                                        Text(
                                            tr("insufficient"),
                                            color = Color(0xFFCE718B),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        "wallet" -> {
                            SectionTitle("🪙 ${tr("walletTitle")}")

                            CuteCard {
                                Text(
                                    tr("balance"),
                                    color = Ink,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "🪙 $coins",
                                    color = DeepSky,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            if (isParent) {
                                CuteCard {
                                    Text(
                                        "🏆 ${tr("exam")}",
                                        color = Ink,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        tr("examRule"),
                                        color = Ink,
                                        lineHeight = 22.sp
                                    )
                                    OutlinedTextField(
                                        value = examScore,
                                        onValueChange = {
                                            examScore = it.filter(Char::isDigit)
                                        },
                                        label = { Text(tr("score")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        singleLine = true
                                    )
                                    CuteButton(
                                        text = "⭐ ${tr("giveReward")}",
                                        onClick = { addExamReward() }
                                    )
                                }
                            }

                            SectionTitle("📖 ${tr("history")}")

                            if (records.isEmpty()) {
                                EmptyCard("📒", tr("noRecords"))
                            }

                            records.forEach { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = CloudWhite
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                record.description,
                                                color = Ink,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                record.date,
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            if (record.amount >= 0)
                                                "+${record.amount}"
                                            else record.amount.toString(),
                                            color = if (record.amount >= 0)
                                                Color(0xFF4AAB78)
                                            else Color(0xFFCE718B),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 本机解除连接，不会删除云端家庭资料
                    TextButton(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "🔗 ${tr("logoutFamily")}",
                            color = Color(0xFFCE718B)
                        )
                    }
                }

                // 操作结果提示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SoftPink
                    )
                ) {
                    Text(
                        message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        color = Ink,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }

                Text(
                    "☁️ Made with love · My Little Rewards ⭐",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = Color(0xFF79AFC9),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text(tr("logoutFamily")) },
            text = { Text(tr("confirmReset")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        disconnectDevice()
                        showDisconnectDialog = false
                    }
                ) {
                    Text(tr("confirm"))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDisconnectDialog = false }
                ) {
                    Text(tr("cancel"))
                }
            }
        )
    }
}

@Composable
private fun CuteCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CloudWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun CuteButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepSky,
            disabledContainerColor = Color(0xFFB9DCEB)
        )
    ) {
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CuteTab(
    modifier: Modifier = Modifier,
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(66.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Sky else CloudWhite,
        shadowElevation = if (selected) 3.dp else 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                textAlign = TextAlign.Center,
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        color = Ink,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun EmptyCard(
    emoji: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Cream
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 30.sp)
            Text(
                text,
                color = Ink,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusText(
    text: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = color
    ) {
        Text(
            text,
            modifier = Modifier.padding(11.dp),
            color = Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
