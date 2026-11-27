package com.mylittlerewards

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val Sky = Color(0xFF8BD5F5)
private val DeepSky = Color(0xFF55B8E8)
private val PaleBlue = Color(0xFFEAF8FF)
private val CloudWhite = Color.White
private val Cream = Color(0xFFFFFBF0)
private val Ink = Color(0xFF34566B)
private val SoftPink = Color(0xFFFFE5EF)
private val SoftYellow = Color(0xFFFFF2C9)
private val SoftGreen = Color(0xFFE3F6E9)
private val DeletePink = Color(0xFFCE718B)

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
    val id: String,
    val description: String,
    val amount: Int,
    val date: String
)

private fun malaysiaDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    return formatter.format(Date())
}

private fun nowText(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    return formatter.format(Date())
}

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
        "delete" to "删除",
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
        "onceTag" to "单次",
        "settings" to "设置",
        "done" to "完成",
        "deleteGift" to "删除礼物",
        "deleteRecord" to "删除记录",
        "clearRecords" to "清除全部记录",
        "confirmDeleteRecord" to "确定删除这笔记录吗？金币余额会同步调整。",
        "confirmClearRecords" to "确定清除所有记录吗？金币余额会按记录总额反向调整。",
        "confirmDeleteGift" to "确定删除这个礼物吗？"
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
        "onceTag" to "Sekali",
        "settings" to "Tetapan",
        "done" to "Selesai",
        "deleteGift" to "Padam hadiah",
        "deleteRecord" to "Padam rekod",
        "clearRecords" to "Padam semua rekod",
        "confirmDeleteRecord" to "Padam rekod ini? Baki syiling akan diselaraskan.",
        "confirmClearRecords" to "Padam semua rekod? Baki syiling akan diselaraskan.",
        "confirmDeleteGift" to "Padam hadiah ini?"
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
        "onceTag" to "Once",
        "settings" to "Settings",
        "done" to "Done",
        "deleteGift" to "Delete gift",
        "deleteRecord" to "Delete record",
        "clearRecords" to "Clear all records",
        "confirmDeleteRecord" to "Delete this record? The coin balance will be adjusted.",
        "confirmClearRecords" to "Delete all records? The coin balance will be adjusted.",
        "confirmDeleteGift" to "Delete this gift?"
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
        "onceTag" to "Một lần",
        "settings" to "Cài đặt",
        "done" to "Xong",
        "deleteGift" to "Xóa quà",
        "deleteRecord" to "Xóa lịch sử",
        "clearRecords" to "Xóa tất cả lịch sử",
        "confirmDeleteRecord" to "Xóa mục này? Số dư xu sẽ được điều chỉnh.",
        "confirmClearRecords" to "Xóa tất cả lịch sử? Số dư xu sẽ được điều chỉnh.",
        "confirmDeleteGift" to "Xóa món quà này?"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RewardsApp(packageName.endsWith(".parent"))
        }
    }
}
@Composable
private fun RewardsApp(isParent: Boolean) {
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences("my_little_rewards", Context.MODE_PRIVATE)
    }
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var uid by remember { mutableStateOf("") }
    var familyCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var inputParentInviteCode by remember { mutableStateOf("") }
    var parentInviteCode by remember { mutableStateOf("") }

    var coins by remember { mutableIntStateOf(0) }
    var tasks by remember { mutableStateOf(emptyList<TaskItem>()) }
    var gifts by remember { mutableStateOf(emptyList<GiftItem>()) }
    var records by remember { mutableStateOf(emptyList<RecordItem>()) }

    // 每日任务完成状态独立储存，不写入 tasks 文件。
    var dailyStatuses by remember {
        mutableStateOf<Map<String, String>>(emptyMap())
    }

    var today by remember {
        mutableStateOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                .apply { timeZone = java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur") }
                .format(Date())
        )
    }

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
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showClearRecordsDialog by remember { mutableStateOf(false) }

    var recordToDelete by remember { mutableStateOf<RecordItem?>(null) }
    var giftToDelete by remember { mutableStateOf<GiftItem?>(null) }

    var familyListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var taskListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var giftListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var recordListener by remember { mutableStateOf<ListenerRegistration?>(null) }
    var dailyListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    fun tr(key: String): String =
        words[language]?.get(key) ?: words["zh"]?.get(key) ?: key

    fun malaysiaDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            .apply {
                timeZone = java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur")
            }
            .format(Date())
    }

    fun dailyCompletionId(taskId: String): String {
        return "${taskId}_$today"
    }

    fun stopListeners() {
        familyListener?.remove()
        taskListener?.remove()
        giftListener?.remove()
        recordListener?.remove()
        dailyListener?.remove()

        familyListener = null
        taskListener = null
        giftListener = null
        recordListener = null
        dailyListener = null
    }

    fun connectFamily(code: String) {
        stopListeners()

        familyCode = code
        today = malaysiaDate()
        dailyStatuses = emptyMap()

        prefs.edit().putString("familyCode", code).apply()

        val family = db.collection("families").document(code)

        familyListener = family.addSnapshotListener { snap, error ->
            if (error != null) {
                message = "家庭资料读取失败：${error.message}"
            } else if (snap != null && snap.exists()) {
                coins = (snap.getLong("coins") ?: 0L).toInt()
            } else {
                message = "找不到家庭资料，请检查配对码。"
            }
        }

        taskListener = family.collection("tasks")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    message = "任务读取失败：${error.message}"
                } else {
                    tasks = snap?.documents?.map { d ->
                        TaskItem(
                            d.id,
                            d.getString("name") ?: "",
                            (d.getLong("coins") ?: 0L).toInt(),
                            d.getBoolean("daily") ?: false,
                            d.getBoolean("submitted") ?: false,
                            d.getBoolean("approved") ?: false
                        )
                    } ?: emptyList()
                }
            }

        giftListener = family.collection("gifts")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    message = "礼物读取失败：${error.message}"
                } else {
                    gifts = snap?.documents?.map { d ->
                        GiftItem(
                            d.id,
                            d.getString("name") ?: "",
                            (d.getLong("price") ?: 0L).toInt(),
                            d.getBoolean("requested") ?: false,
                            d.getBoolean("approved") ?: false
                        )
                    } ?: emptyList()
                }
            }

        recordListener = family.collection("records")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    message = "记录读取失败：${error.message}"
                } else {
                    records = snap?.documents?.map { d ->
                        RecordItem(
                            d.id,
                            d.getString("description") ?: "",
                            (d.getLong("amount") ?: 0L).toInt(),
                            d.getString("date") ?: ""
                        )
                    } ?: emptyList()
                }
            }

        // 只读取今天的每日任务完成记录。
        dailyListener = family.collection("dailyCompletions")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    message = "每日任务状态读取失败：${error.message}"
                } else {
                    val prefix = "_$today"
                    dailyStatuses = snap?.documents
                        ?.filter { it.id.endsWith(prefix) }
                        ?.associate { d ->
                            val taskId = d.id.removeSuffix(prefix)
                            taskId to (d.getString("status") ?: "")
                        } ?: emptyMap()
                }
            }

        message = "已连接家庭：$code"
    }

    // 重新进入前台时检查日期，跨过午夜后自动显示新一天的任务。
    LaunchedEffect(familyCode) {
        if (familyCode.isNotBlank()) {
            while (true) {
                kotlinx.coroutines.delay(30_000)
                val newDate = malaysiaDate()

                if (newDate != today) {
                    today = newDate
                    dailyStatuses = emptyMap()
                    dailyListener?.remove()

                    dailyListener = db.collection("families")
                        .document(familyCode)
                        .collection("dailyCompletions")
                        .addSnapshotListener { snap, error ->
                            if (error != null) {
                                message = "每日任务状态读取失败：${error.message}"
                            } else {
                                val prefix = "_$today"
                                dailyStatuses = snap?.documents
                                    ?.filter { it.id.endsWith(prefix) }
                                    ?.associate { d ->
                                        d.id.removeSuffix(prefix) to
                                            (d.getString("status") ?: "")
                                    } ?: emptyMap()
                            }
                        }
                }
            }
        }
    }

    fun dailyStatus(task: TaskItem): String {
        return if (task.daily) {
            dailyStatuses[task.id] ?: ""
        } else {
            when {
                task.approved -> "approved"
                task.submitted -> "submitted"
                else -> ""
            }
        }
    }

    fun isTaskSubmitted(task: TaskItem): Boolean =
        dailyStatus(task) == "submitted" ||
            dailyStatus(task) == "approved"

    fun isTaskApproved(task: TaskItem): Boolean =
        dailyStatus(task) == "approved"

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
            val saved = prefs.getString("familyCode", "") ?: ""

            if (saved.isNotBlank()) {
                connectFamily(saved)
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
        onDispose {
            stopListeners()
        }
    }
        // 创建新家庭
    fun createFamily() {
        if (uid.isBlank()) {
            message = "账号尚未准备好，请稍等。"
            return
        }

        loading = true

        val code = (1..6)
            .map { SecureRandom().nextInt(10) }
            .joinToString("")

        val familyData = hashMapOf(
            "ownerUid" to uid,
            "parentUids" to listOf(uid),
            "childUid" to "",
            "coins" to 0,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("families")
            .document(code)
            .set(familyData)
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

    // 孩子加入家庭
    fun joinFamily(code: String) {
        val cleanCode = code.trim()

        if (cleanCode.isBlank()) {
            message = "请先输入家庭配对码。"
            return
        }

        if (uid.isBlank()) {
            message = "账号尚未准备好，请稍等。"
            return
        }

        loading = true

        val familyRef = db.collection("families").document(cleanCode)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(familyRef)

            if (!snapshot.exists()) {
                throw Exception("找不到这个家庭配对码。")
            }

            val existingChild = snapshot.getString("childUid") ?: ""

            if (existingChild.isNotBlank() && existingChild != uid) {
                throw Exception("这个家庭已经连接了孩子账号。")
            }

            transaction.update(familyRef, "childUid", uid)
            null
        }.addOnSuccessListener {
            loading = false
            connectFamily(cleanCode)
            message = "成功加入家庭！"
        }.addOnFailureListener {
            loading = false
            message = "加入失败：${it.message}"
        }
    }

    // 家长产生第二位家长邀请码
    fun generateParentInvite() {
        if (!isParent || familyCode.isBlank()) {
            message = "请先连接家庭。"
            return
        }

        val invite = (100000..999999)
            .random()
            .toString()

        val inviteRef = db.collection("parentInvites").document(invite)
        val familyRef = db.collection("families").document(familyCode)

        db.runTransaction { transaction ->
            val familySnapshot = transaction.get(familyRef)

            if (!familySnapshot.exists()) {
                throw Exception("找不到家庭资料。")
            }

            val parents = familySnapshot.get("parentUids") as? List<*>
                ?: listOf(familySnapshot.getString("ownerUid") ?: "")

            if (parents.filterIsInstance<String>().distinct().size >= 2) {
                throw Exception("这个家庭已经有两位家长。")
            }

            transaction.set(
                inviteRef,
                mapOf(
                    "familyId" to familyCode,
                    "createdBy" to uid,
                    "used" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "expiresAt" to Date(System.currentTimeMillis() + 15 * 60 * 1000)
                )
            )
            null
        }.addOnSuccessListener {
            parentInviteCode = invite
            message = "邀请码已生成，有效期15分钟。"
        }.addOnFailureListener {
            message = "产生邀请码失败：${it.message}"
        }
    }

    // 第二位家长使用邀请码加入
    fun joinAsSecondParent(inviteCode: String) {
        val cleanInvite = inviteCode.trim()

        if (cleanInvite.isBlank() || uid.isBlank()) {
            message = "请输入有效邀请码。"
            return
        }

        loading = true

        val inviteRef = db.collection("parentInvites").document(cleanInvite)

        db.runTransaction { transaction ->
            val inviteSnapshot = transaction.get(inviteRef)

            if (!inviteSnapshot.exists()) {
                throw Exception("邀请码不存在。")
            }

            if (inviteSnapshot.getBoolean("used") == true) {
                throw Exception("这个邀请码已经使用。")
            }

            val expiresAt = inviteSnapshot.getTimestamp("expiresAt")
            if (expiresAt == null || expiresAt.toDate().before(Date())) {
                throw Exception("邀请码已经过期。")
            }

            val targetFamily = inviteSnapshot.getString("familyId")
                ?: throw Exception("邀请码资料不完整。")

            val familyRef = db.collection("families").document(targetFamily)
            val familySnapshot = transaction.get(familyRef)

            if (!familySnapshot.exists()) {
                throw Exception("找不到家庭资料。")
            }

            val parents = (
                familySnapshot.get("parentUids") as? List<*>
                    ?: listOf(familySnapshot.getString("ownerUid") ?: "")
                ).filterIsInstance<String>().distinct()

            if (parents.size >= 2 && uid !in parents) {
                throw Exception("这个家庭已经有两位家长。")
            }

            transaction.update(
                familyRef,
                "parentUids",
                (parents + uid).distinct()
            )
            transaction.update(inviteRef, "used", true)

            targetFamily
        }.addOnSuccessListener { targetFamily ->
            loading = false
            connectFamily(targetFamily)
            message = "成功连接为第二位家长！"
        }.addOnFailureListener {
            loading = false
            message = "加入失败：${it.message}"
        }
    }

    // 新增任务
    fun addTask() {
        if (!isParent || familyCode.isBlank()) return

        val name = taskName.trim()
        val amount = taskCoins.toIntOrNull()

        if (name.isBlank() || amount == null || amount <= 0) {
            message = "请填写任务名称和有效金币数量。"
            return
        }

        val data = hashMapOf(
            "name" to name,
            "coins" to amount,
            "daily" to daily,
            "submitted" to false,
            "approved" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("families")
            .document(familyCode)
            .collection("tasks")
            .add(data)
            .addOnSuccessListener {
                taskName = ""
                taskCoins = "5"
                daily = true
                message = "任务添加成功！"
            }
            .addOnFailureListener {
                message = "添加任务失败：${it.message}"
            }
    }

    // 孩子提交任务
    fun submitTask(task: TaskItem) {
        if (isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val taskRef = familyRef.collection("tasks").document(task.id)

        if (task.daily) {
            val completionRef = familyRef.collection("dailyCompletions")
                .document("${task.id}_${today}")

            db.runTransaction { transaction ->
                val completion = transaction.get(completionRef)

                if (completion.exists()) {
                    throw Exception("今天已经提交过这个任务了。")
                }

                transaction.set(
                    completionRef,
                    mapOf(
                        "taskId" to task.id,
                        "date" to today,
                        "status" to "submitted",
                        "submittedBy" to uid,
                        "submittedAt" to FieldValue.serverTimestamp()
                    )
                )
                null
            }.addOnSuccessListener {
                message = "任务已提交，等待家长批准！"
            }.addOnFailureListener {
                message = "提交失败：${it.message}"
            }
        } else {
            taskRef.update("submitted", true)
                .addOnSuccessListener {
                    message = "任务已提交，等待家长批准！"
                }
                .addOnFailureListener {
                    message = "提交失败：${it.message}"
                }
        }
    }

    // 家长批准任务
    fun approveTask(task: TaskItem) {
        if (!isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val taskRef = familyRef.collection("tasks").document(task.id)
        val recordRef = familyRef.collection("records").document()

        if (task.daily) {
            val completionRef = familyRef.collection("dailyCompletions")
                .document("${task.id}_${today}")

            db.runTransaction { transaction ->
                val completion = transaction.get(completionRef)
                val familySnapshot = transaction.get(familyRef)
                val taskSnapshot = transaction.get(taskRef)

                if (!completion.exists()) {
                    throw Exception("今天还没有提交这个任务。")
                }

                if (completion.getString("status") != "submitted") {
                    throw Exception("这个任务已经处理过了。")
                }

                if (!taskSnapshot.exists()) {
                    throw Exception("找不到任务资料。")
                }

                if (!familySnapshot.exists()) {
                    throw Exception("找不到家庭资料。")
                }

                val reward = (taskSnapshot.getLong("coins") ?: 0L)

                transaction.update(
                    completionRef,
                    mapOf(
                        "status" to "approved",
                        "approvedBy" to uid,
                        "approvedAt" to FieldValue.serverTimestamp()
                    )
                )

                transaction.update(
                    familyRef,
                    "coins",
                    FieldValue.increment(reward)
                )

                transaction.set(
                    recordRef,
                    mapOf(
                        "description" to "每日任务：${taskSnapshot.getString("name") ?: ""}",
                        "amount" to reward,
                        "date" to nowText(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                null
            }.addOnSuccessListener {
                message = "批准成功，金币已经发放！"
            }.addOnFailureListener {
                message = "批准失败：${it.message}"
            }
        } else {
            db.runTransaction { transaction ->
                val taskSnapshot = transaction.get(taskRef)
                val familySnapshot = transaction.get(familyRef)

                if (!taskSnapshot.exists()) {
                    throw Exception("找不到任务资料。")
                }

                if (taskSnapshot.getBoolean("submitted") != true) {
                    throw Exception("孩子还没有提交这个任务。")
                }

                if (taskSnapshot.getBoolean("approved") == true) {
                    throw Exception("这个任务已经批准过了。")
                }

                if (!familySnapshot.exists()) {
                    throw Exception("找不到家庭资料。")
                }

                val reward = taskSnapshot.getLong("coins") ?: 0L
                val name = taskSnapshot.getString("name") ?: ""

                transaction.update(taskRef, "approved", true)
                transaction.update(
                    familyRef,
                    "coins",
                    FieldValue.increment(reward)
                )

                transaction.set(
                    recordRef,
                    mapOf(
                        "description" to "一次性任务：$name",
                        "amount" to reward,
                        "date" to nowText(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                null
            }.addOnSuccessListener {
                message = "批准成功，金币已经发放！"
            }.addOnFailureListener {
                message = "批准失败：${it.message}"
            }
        }
    }

    // 删除任务
    fun deleteTask(task: TaskItem) {
        if (!isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val taskRef = familyRef.collection("tasks").document(task.id)

        taskRef.delete()
            .addOnSuccessListener {
                // 删除任务本身；历史每日完成记录保留，不删除 Wallet 历史。
                message = "任务已删除。"
            }
            .addOnFailureListener {
                message = "删除失败：${it.message}"
            }
    }
        // 新增礼物
    fun addGift() {
        if (!isParent || familyCode.isBlank()) return

        val name = giftName.trim()
        val price = giftPrice.toIntOrNull()

        if (name.isBlank() || price == null || price <= 0) {
            message = "请填写礼物名称和有效金币价格。"
            return
        }

        val data = hashMapOf(
            "name" to name,
            "price" to price,
            "requested" to false,
            "approved" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("families")
            .document(familyCode)
            .collection("gifts")
            .add(data)
            .addOnSuccessListener {
                giftName = ""
                giftPrice = "20"
                message = "礼物添加成功！"
            }
            .addOnFailureListener {
                message = "添加礼物失败：${it.message}"
            }
    }

    // 删除礼物
    fun deleteGift(gift: GiftItem) {
        if (!isParent || familyCode.isBlank()) return

        db.collection("families")
            .document(familyCode)
            .collection("gifts")
            .document(gift.id)
            .delete()
            .addOnSuccessListener {
                giftToDelete = null
                message = "礼物已删除。"
            }
            .addOnFailureListener {
                message = "删除礼物失败：${it.message}"
            }
    }

    // 孩子申请兑换礼物
    fun requestGift(gift: GiftItem) {
        if (isParent || familyCode.isBlank()) return

        if (gift.requested || gift.approved) {
            message = "这个礼物已经申请过了。"
            return
        }

        db.collection("families")
            .document(familyCode)
            .collection("gifts")
            .document(gift.id)
            .update(
                mapOf(
                    "requested" to true,
                    "requestedBy" to uid,
                    "requestedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener {
                message = "礼物申请已送出，等待家长批准！"
            }
            .addOnFailureListener {
                message = "申请失败：${it.message}"
            }
    }

    // 家长批准礼物兑换
    fun approveGift(gift: GiftItem) {
        if (!isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val giftRef = familyRef.collection("gifts").document(gift.id)
        val recordRef = familyRef.collection("records").document()

        db.runTransaction { transaction ->
            val giftSnapshot = transaction.get(giftRef)
            val familySnapshot = transaction.get(familyRef)

            if (!giftSnapshot.exists()) {
                throw Exception("找不到礼物资料。")
            }

            if (giftSnapshot.getBoolean("requested") != true) {
                throw Exception("孩子还没有申请这个礼物。")
            }

            if (giftSnapshot.getBoolean("approved") == true) {
                throw Exception("这个礼物已经兑换过了。")
            }

            if (!familySnapshot.exists()) {
                throw Exception("找不到家庭资料。")
            }

            val price = giftSnapshot.getLong("price") ?: 0L
            val currentCoins = familySnapshot.getLong("coins") ?: 0L
            val name = giftSnapshot.getString("name") ?: ""

            if (currentCoins < price) {
                throw Exception("孩子的金币不足。")
            }

            transaction.update(
                giftRef,
                mapOf(
                    "approved" to true,
                    "approvedBy" to uid,
                    "approvedAt" to FieldValue.serverTimestamp()
                )
            )

            transaction.update(
                familyRef,
                "coins",
                FieldValue.increment(-price)
            )

            transaction.set(
                recordRef,
                mapOf(
                    "description" to "兑换礼物：$name",
                    "amount" to -price,
                    "date" to nowText(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.addOnSuccessListener {
            message = "礼物兑换成功！"
        }.addOnFailureListener {
            message = "兑换失败：${it.message}"
        }
    }

    // 考试奖励
    fun addExamReward() {
        if (!isParent || familyCode.isBlank()) return

        val score = examScore.toIntOrNull()

        if (score == null || score !in 0..100) {
            message = "请输入 0 至 100 之间的考试分数。"
            return
        }

        val reward = when (score) {
            in 90..100 -> 30
            in 80..89 -> 20
            in 70..79 -> 10
            else -> 0
        }

        if (reward == 0) {
            message = "这次考试没有获得金币奖励。"
            examScore = ""
            return
        }

        val familyRef = db.collection("families").document(familyCode)
        val recordRef = familyRef.collection("records").document()

        db.runTransaction { transaction ->
            val familySnapshot = transaction.get(familyRef)

            if (!familySnapshot.exists()) {
                throw Exception("找不到家庭资料。")
            }

            transaction.update(
                familyRef,
                "coins",
                FieldValue.increment(reward.toLong())
            )

            transaction.set(
                recordRef,
                mapOf(
                    "description" to "考试奖励：$score 分",
                    "amount" to reward,
                    "date" to nowText(),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )

            null
        }.addOnSuccessListener {
            examScore = ""
            message = "考试奖励已发放：+$reward 金币！"
        }.addOnFailureListener {
            message = "考试奖励失败：${it.message}"
        }
    }

    // 删除单笔 Wallet 记录，并调整金币余额
    fun deleteRecord(record: RecordItem) {
        if (!isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val recordRef = familyRef.collection("records").document(record.id)

        db.runTransaction { transaction ->
            val recordSnapshot = transaction.get(recordRef)
            val familySnapshot = transaction.get(familyRef)

            if (!recordSnapshot.exists()) {
                throw Exception("这笔记录已经不存在。")
            }

            if (!familySnapshot.exists()) {
                throw Exception("找不到家庭资料。")
            }

            val amount = recordSnapshot.getLong("amount") ?: 0L

            transaction.update(
                familyRef,
                "coins",
                FieldValue.increment(-amount)
            )

            transaction.delete(recordRef)
            null
        }.addOnSuccessListener {
            recordToDelete = null
            message = "记录已删除，金币余额已调整。"
        }.addOnFailureListener {
            message = "删除记录失败：${it.message}"
        }
    }

    // 清除全部 Wallet 记录，并调整金币余额
    fun clearRecords() {
        if (!isParent || familyCode.isBlank()) return

        val familyRef = db.collection("families").document(familyCode)
        val recordsRef = familyRef.collection("records")

        loading = true

        recordsRef.get()
            .addOnSuccessListener { snapshot ->
                val documents = snapshot.documents

                if (documents.isEmpty()) {
                    loading = false
                    showClearRecordsDialog = false
                    message = "目前没有 Wallet 记录。"
                    return@addOnSuccessListener
                }

                if (documents.size > 450) {
                    loading = false
                    message = "记录数量太多，请分批清除。"
                    return@addOnSuccessListener
                }

                db.runTransaction { transaction ->
                    val familySnapshot = transaction.get(familyRef)

                    if (!familySnapshot.exists()) {
                        throw Exception("找不到家庭资料。")
                    }

                    var totalAmount = 0L

                    documents.forEach { document ->
                        val recordSnapshot = transaction.get(
                            recordsRef.document(document.id)
                        )

                        if (recordSnapshot.exists()) {
                            totalAmount += recordSnapshot.getLong("amount") ?: 0L
                            transaction.delete(
                                recordsRef.document(document.id)
                            )
                        }
                    }

                    transaction.update(
                        familyRef,
                        "coins",
                        FieldValue.increment(-totalAmount)
                    )

                    null
                }.addOnSuccessListener {
                    loading = false
                    showClearRecordsDialog = false
                    message = "Wallet 记录已清除，余额已重新调整。"
                }.addOnFailureListener {
                    loading = false
                    message = "清除失败：${it.message}"
                }
            }
            .addOnFailureListener {
                loading = false
                message = "读取 Wallet 记录失败：${it.message}"
            }
    }

    // 断开当前设备与家庭的连接
    fun disconnectDevice() {
        stopListeners()

        prefs.edit()
            .remove("familyCode")
            .apply()

        familyCode = ""
        coins = 0
        tasks = emptyList()
        gifts = emptyList()
        records = emptyList()
        dailyStatuses = emptyMap()

        inputCode = ""
        inputParentInviteCode = ""
        parentInviteCode = ""

        showDisconnectDialog = false
        message = "此设备已断开家庭连接。"
    }
        // 主界面
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaleBlue)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 首页标题
        Text(
            text = "My Little Rewards",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(R.drawable.cinnamoroll_home),
            contentDescription = "Cinnamoroll",
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 金币余额
        CuteCard {
            Text(
                text = "⭐ My Coins ⭐",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                text = "$coins",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = DeepSky
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 家庭连接
        if (familyCode.isBlank()) {
            CuteCard {
                Text(
                    text = if (isParent) "Parent Account" else "Child Account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    color = Ink,
                    textAlign = TextAlign.Center
                )

                if (isParent) {
                    CuteButton(
                        text = "Create New Family",
                        onClick = { createFamily() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Family Code") },
                    singleLine = true
                )

                CuteButton(
                    text = "Join Family",
                    onClick = { joinFamily(inputCode) }
                )

                if (isParent) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputParentInviteCode,
                        onValueChange = {
                            inputParentInviteCode = it
                        },
                        label = { Text("Second Parent Invite Code") },
                        singleLine = true
                    )

                    CuteButton(
                        text = "Join as Second Parent",
                        onClick = {
                            joinAsSecondParent(inputParentInviteCode)
                        }
                    )
                }
            }
        } else {
            Text(
                text = "Family Code: $familyCode",
                fontSize = 14.sp,
                color = Ink
            )

            Text(
                text = message,
                fontSize = 13.sp,
                color = DeepSky,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 页面标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CuteTab(
                    text = "Tasks",
                    selected = selectedTab == "tasks",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "tasks" }
                )

                CuteTab(
                    text = "Gifts",
                    selected = selectedTab == "gifts",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "gifts" }
                )

                CuteTab(
                    text = "Wallet",
                    selected = selectedTab == "wallet",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "wallet" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================
            // TASKS PAGE
            // =========================
            if (selectedTab == "tasks") {

                if (isParent) {
                    CuteCard {
                        SectionTitle("Add New Task")

                        OutlinedTextField(
                            value = taskName,
                            onValueChange = { taskName = it },
                            label = { Text("Task Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = taskCoins,
                            onValueChange = { taskCoins = it },
                            label = { Text("Coins") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = daily,
                                onCheckedChange = { daily = it }
                            )

                            Text(
                                text = "Daily Task",
                                color = Ink
                            )
                        }

                        CuteButton(
                            text = "Add Task",
                            onClick = { addTask() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 可完成任务
                SectionTitle("Available Tasks")

                val availableTasks = tasks.filter { task ->
                    if (task.daily) {
                        dailyStatus(task).isBlank()
                    } else {
                        !task.submitted && !task.approved
                    }
                }

                if (availableTasks.isEmpty()) {
                    EmptyCard("No available tasks")
                }

                availableTasks.forEach { task ->
                    CuteCard {
                        Text(
                            text = task.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Text(
                            text = "+${task.coins} Coins",
                            color = DeepSky
                        )

                        Text(
                            text = if (task.daily) {
                                "Daily Task"
                            } else {
                                "One-time Task"
                            },
                            color = Ink
                        )

                        if (isParent) {
                            CuteButton(
                                text = "Delete Task",
                                onClick = { deleteTask(task) }
                            )
                        } else {
                            CuteButton(
                                text = "Submit Task",
                                onClick = { submitTask(task) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 等待批准任务
                SectionTitle("Awaiting Approval")

                val awaitingTasks = tasks.filter { task ->
                    dailyStatus(task) == "submitted"
                }

                if (awaitingTasks.isEmpty()) {
                    EmptyCard("No tasks awaiting approval")
                }

                awaitingTasks.forEach { task ->
                    CuteCard {
                        Text(
                            text = task.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Text(
                            text = "+${task.coins} Coins",
                            color = DeepSky
                        )

                        StatusText("Awaiting Approval")

                        if (isParent) {
                            CuteButton(
                                text = "Approve Task",
                                onClick = { approveTask(task) }
                            )
                        }
                    }
                }

                // 一次性任务和每日任务都可以在这里查看状态。
                // 已批准任务不会再出现在今天的任务列表。
            }

            // =========================
            // GIFTS PAGE
            // =========================
            if (selectedTab == "gifts") {

                if (isParent) {
                    CuteCard {
                        SectionTitle("Add New Gift")

                        OutlinedTextField(
                            value = giftName,
                            onValueChange = { giftName = it },
                            label = { Text("Gift Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = giftPrice,
                            onValueChange = { giftPrice = it },
                            label = { Text("Gift Price") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        CuteButton(
                            text = "Add Gift",
                            onClick = { addGift() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle("Gift Shop")

                if (gifts.isEmpty()) {
                    EmptyCard("No gifts available")
                }

                gifts.forEach { gift ->
                    CuteCard {
                        Text(
                            text = gift.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Text(
                            text = "${gift.price} Coins",
                            color = DeepSky
                        )

                        when {
                            gift.approved -> StatusText("Redeemed")
                            gift.requested -> StatusText("Awaiting Approval")
                            else -> StatusText("Available")
                        }

                        if (isParent) {
                            if (gift.requested && !gift.approved) {
                                CuteButton(
                                    text = "Approve Redemption",
                                    onClick = { approveGift(gift) }
                                )
                            }

                            CuteButton(
                                text = "Delete Gift",
                                onClick = { giftToDelete = gift }
                            )
                        } else if (!gift.requested && !gift.approved) {
                            CuteButton(
                                text = "Request Gift",
                                onClick = { requestGift(gift) }
                            )
                        }
                    }
                }
            }

            // =========================
            // WALLET PAGE
            // =========================
            if (selectedTab == "wallet") {

                CuteCard {
                    SectionTitle("My Wallet")

                    Text(
                        text = "$coins Coins",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepSky
                    )
                }

                if (isParent) {
                    Spacer(modifier = Modifier.height(12.dp))

                    CuteCard {
                        SectionTitle("Exam Reward")

                        OutlinedTextField(
                            value = examScore,
                            onValueChange = { examScore = it },
                            label = { Text("Exam Score (0-100)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "90-100: 30 Coins\n80-89: 20 Coins\n70-79: 10 Coins\nBelow 70: 0 Coins",
                            color = Ink
                        )

                        CuteButton(
                            text = "Give Exam Reward",
                            onClick = { addExamReward() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SectionTitle("Transaction History")

                if (records.isEmpty()) {
                    EmptyCard("No wallet records")
                }

                records.forEach { record ->
                    CuteCard {
                        Text(
                            text = record.description,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Text(
                            text = record.date,
                            color = Ink
                        )

                        Text(
                            text = if (record.amount >= 0) {
                                "+${record.amount} Coins"
                            } else {
                                "${record.amount} Coins"
                            },
                            color = if (record.amount >= 0) {
                                Color(0xFF36A66B)
                            } else {
                                DeletePink
                            }
                        )

                        if (isParent) {
                            CuteButton(
                                text = "Delete Record",
                                onClick = { recordToDelete = record }
                            )
                        }
                    }
                }

                if (isParent && records.isNotEmpty()) {
                    CuteButton(
                        text = "Clear All Records",
                        onClick = { showClearRecordsDialog = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
        // =========================
    // SETTINGS BUTTON
    // =========================

    if (familyCode.isNotBlank()) {
        CuteButton(
            text = "⚙ Settings",
            onClick = {
                showSettingsDialog = true
            }
        )
    }

    // =========================
    // SETTINGS DIALOG
    // =========================

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSettingsDialog = false
            },
            title = {
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    Text(
                        text = "Language",
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "zh" to "中文",
                        "en" to "English",
                        "ms" to "Bahasa Melayu",
                        "vi" to "Tiếng Việt"
                    ).forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = language == item.first,
                                onClick = {
                                    language = item.first
                                    prefs.edit()
                                        .putString("language", item.first)
                                        .apply()
                                }
                            )

                            Text(
                                text = item.second,
                                color = Ink
                            )
                        }
                    }

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Family Information",
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )

                    Text(
                        text = "Family Code: $familyCode",
                        color = Ink
                    )

                    if (isParent) {
                        Spacer(modifier = Modifier.height(8.dp))

                        CuteButton(
                            text = "Generate Second Parent Invite",
                            onClick = {
                                generateParentInvite()
                            }
                        )

                        if (parentInviteCode.isNotBlank()) {
                            Text(
                                text = "Invite Code: $parentInviteCode",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepSky
                            )

                            Text(
                                text = "Valid for 15 minutes",
                                color = Ink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CuteButton(
                        text = "Disconnect This Device",
                        onClick = {
                            showSettingsDialog = false
                            showDisconnectDialog = true
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSettingsDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // =========================
    // DELETE WALLET RECORD DIALOG
    // =========================

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = {
                recordToDelete = null
            },
            title = {
                Text("Delete Wallet Record?")
            },
            text = {
                Text(
                    "Delete this transaction?\n\n" +
                        "${record.description}\n" +
                        "${record.amount} Coins\n\n" +
                        "The coin balance will also be adjusted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteRecord(record)
                    }
                ) {
                    Text(
                        "Delete",
                        color = DeletePink
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        recordToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================
    // CLEAR WALLET RECORDS DIALOG
    // =========================

    if (showClearRecordsDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearRecordsDialog = false
            },
            title = {
                Text("Clear All Wallet Records?")
            },
            text = {
                Text(
                    "All Wallet transaction records will be deleted. " +
                        "The coin balance will be adjusted according to " +
                        "the deleted transactions."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        clearRecords()
                    }
                ) {
                    Text(
                        "Clear All",
                        color = DeletePink
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearRecordsDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================
    // DELETE GIFT DIALOG
    // =========================

    giftToDelete?.let { gift ->
        AlertDialog(
            onDismissRequest = {
                giftToDelete = null
            },
            title = {
                Text("Delete Gift?")
            },
            text = {
                Text(
                    "Are you sure you want to delete ${gift.name}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteGift(gift)
                    }
                ) {
                    Text(
                        "Delete",
                        color = DeletePink
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        giftToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // =========================
    // DISCONNECT DEVICE DIALOG
    // =========================

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = {
                showDisconnectDialog = false
            },
            title = {
                Text("Disconnect This Device?")
            },
            text = {
                Text(
                    "This device will disconnect from the family. " +
                        "Family data and other devices will not be deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        disconnectDevice()
                    }
                ) {
                    Text(
                        "Disconnect",
                        color = DeletePink
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDisconnectDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // =========================
// TIME HELPER
// =========================

private fun nowText(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault()
    ).apply {
        timeZone = java.util.TimeZone.getTimeZone("Asia/Kuala_Lumpur")
    }.format(Date())
}

// =========================
// CUTE CARD
// =========================

@Composable
private fun CuteCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CloudWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// =========================
// CUTE BUTTON
// =========================

@Composable
private fun CuteButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DeepSky,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

// =========================
// CUTE TAB
// =========================

@Composable
private fun CuteTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) DeepSky else Sky,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(
            horizontal = 6.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

// =========================
// SECTION TITLE
// =========================

@Composable
private fun SectionTitle(
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                R.drawable.cinnamoroll_mini
            ),
            contentDescription = "Cinnamoroll",
            modifier = Modifier.size(38.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
    }
}

// =========================
// EMPTY CARD
// =========================

@Composable
private fun EmptyCard(
    text: String
) {
    CuteCard {
        Text(
            text = text,
            color = Ink,
            textAlign = TextAlign.Center
        )
    }
}

// =========================
// STATUS TEXT
// =========================

@Composable
private fun StatusText(
    text: String
) {
    Text(
        text = "● $text",
        color = DeepSky,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}
