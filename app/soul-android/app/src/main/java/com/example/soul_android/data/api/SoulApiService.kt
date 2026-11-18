package com.example.soul_android.data.api

import com.example.soul_android.BuildConfig
import com.example.soul_android.data.ServerConfig
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * 用户登录请求数据类。
 * @property username 用户名
 * @property password 登录密码
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 用户登录响应数据类。
 * @property success 是否登录成功
 * @property name 用户真实姓名
 * @property username 用户名
 * @property email 电子邮箱
 * @property avatar 头像地址
 * @property role 用户角色
 * @property message 登录提示或错误信息
 */
data class LoginResponse(
    val success: Boolean,
    val name: String?,
    val username: String?,
    val email: String?,
    val avatar: String?,
    val role: String?,
    val message: String?
)

/**
 * 用户注册请求数据类。
 * @property username 用户名
 * @property name 姓名
 * @property email 电子邮箱
 * @property password 密码
 * @property age 年龄
 * @property gender 性别
 * @property phone 电话号码
 * @property nationality 国籍
 * @property address 地址
 * @property timeSlot 可用时间段
 * @property avatar 头像地址
 */
data class RegisterRequest(
    val username: String,
    val name: String,
    val email: String,
    val password: String,

    val age: Int? = null,
    val gender: String? = null,
    val phone: String? = null,
    val nationality: String? = null,
    val address: String? = null,
    val timeSlot: String? = null,
    val avatar: String? = null
)

/**
 * 用户名查重响应数据类。
 * @property available 用户名是否可用
 * @property message 提示信息
 */
data class UsernameCheckResponse(
    val available: Boolean?,
    val message: String?
)

/**
 * 文件上传响应数据类。
 * @property url 上传成功后的访问URL
 */
data class UploadUrlResponse(
    val url: String?
)

/**
 * 用户详细资料响应数据类。
 */
data class ProfileResponse(
    val id: Long?,
    val username: String,
    val name: String?,
    val email: String?,
    val avatar: String?,
    val gender: String?,
    val age: Int?,

    val skillOffer: String?,
    val skillWant: String?,

    val skillOfferLevel: String?,
    val skillWantLevel: String?,

    val nationality: String?,

    val averageRating: Double?,
    val ratingCount: Int? = null,

    val role: String?,

    val phone: String?,
    val address: String?,

    val timeSlot: String? = null
)

/**
 * 头像上传响应数据类。
 */
data class AvatarUploadResponse(
    val success: Boolean,
    val avatar: String?,
    val message: String?
)

/**
 * AI 指引/指南响应数据类。
 */
data class AiGuideResponse(
    val advice: String
)

/**
 * 匹配用户响应数据类。
 */
data class MatchUserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val age: Int?,
    val gender: String?,
    val nationality: String?,
    val avatar: String?,
    val skillOffer: List<String>? = null,
    val skillWant: List<String>? = null,
    val timeSlot: String?,
    val skillWantLevel: String?,
    val skillOfferLevel: String?,
    val averageRating: Double?,
    val ratingCount: Int?
)

/**
 * 匹配历史记录响应数据类。
 */
data class MatchHistoryResponse(
    val partnerId: Long?,
    val partnerName: String?,
    val partnerAvatar: String?,
    val matchTime: String?
)

/**
 * 用户匹配档案响应数据类（包含历史记录）。
 */
data class UserMatchProfileResponse(
    val id: Long,
    val username: String,
    val name: String?,
    val age: Int?,
    val nationality: String?,
    val avatar: String?,
    val skillOffer: String?,
    val skillWant: String?,
    val averageRating: Double?,
    val ratingCount: Int?,
    val histories: List<MatchHistoryResponse>? = emptyList()
)

/**
 * 聊天消息发送请求数据类。
 */
data class ChatSendRequest(
    val senderUsername: String,
    val receiverUsername: String,
    val content: String,
    val type: String = "TEXT",
    val mediaUrl: String? = null
)

/**
 * 后端聊天消息数据类。
 */
data class BackendChatMessage(
    val id: Long?,
    val senderUsername: String,
    val receiverUsername: String,
    val content: String,
    val createdAt: String?,
    val type: String = "TEXT",
    val mediaUrl: String? = null,
    val recalled: Boolean = false
)

/**
 * 聊天消息发送响应数据类。
 */
data class ChatSendResponse(
    val success: Boolean,
    val message: String?,
    val data: BackendChatMessage?
)

/**
 * 消息操作请求数据类（如撤回、删除）。
 */
data class MessageActionRequest(
    val messageId: Long,
    val username: String
)

/**
 * 消息已读标记请求数据类。
 */
data class ReadRequest(
    val username: String,
    val partner: String
)

/**
 * 音视频通话发起请求数据类。
 */
data class CallStartRequest(
    val caller: String,
    val callee: String,
    val callType: String
)

/**
 * 音视频通话应答请求数据类。
 */
data class CallAnswerRequest(
    val callId: String,
    val callee: String,
    val accept: Boolean
)

/**
 * 音视频通话结束请求数据类。
 */
data class CallEndRequest(
    val callId: String,
    val caller: String
)

/**
 * 音视频通话状态响应数据类。
 */
data class CallStatusResponse(
    val callId: String?,
    val caller: String?,
    val callee: String?,
    val channelName: String?,
    val callType: String?,
    val status: String?,
    val callerName: String? = null
)

/**
 * 声网通话Token响应数据类。
 */
data class CallTokenResponse(
    val token: String?,
    val appId: String?,
    val uid: Int?,
    val channelName: String?
)

/**
 * 用户关系状态数据类。
 */
data class RelationStatus(
    val matched: Boolean = false,
    val friend: Boolean = false,
    val blockedByMe: Boolean = false,
    val blockedByOther: Boolean = false
)

/**
 * 好友备注设置请求数据类。
 */
data class RemarkRequest(
    val owner: String,
    val target: String,
    val remark: String
)

/**
 * 好友申请请求数据类。
 */
data class FriendRequest(
    val username: String,
    val friendUsername: String,
    val message: String? = null
)

/**
 * 好友申请响应请求数据类。
 */
data class FriendRespondRequest(
    val requestId: Long,
    val username: String,
    val accept: Boolean
)

/**
 * 好友申请记录响应数据类。
 */
data class FriendRequestResponse(
    val requestId: Long?,
    val fromUser: String?,
    val fromName: String?,
    val message: String?,
    val createdAt: String?
)

/**
 * 用户拉黑请求数据类。
 */
data class BlockRequest(
    val username: String,
    val blockUsername: String
)

/**
 * 语聊房创建请求数据类。
 */
data class RoomCreateRequest(
    val owner: String,
    val roomName: String
)

/**
 * 语聊房邀请请求数据类。
 */
data class RoomInviteRequest(
    val roomId: String,
    val inviter: String,
    val invitee: String
)

/**
 * 语聊房邀请响应请求数据类。
 */
data class RoomRespondRequest(
    val roomId: String,
    val username: String,
    val accept: Boolean
)

/**
 * 语聊房消息发送请求数据类。
 */
data class RoomMessageRequest(
    val roomId: String,
    val sender: String,
    val content: String
)

/**
 * 语聊房参与者信息数据类。
 */
data class RoomParticipant(
    val username: String?,
    val name: String?,
    val avatar: String?
)

/**
 * 语聊房状态响应数据类。
 */
data class RoomStatusResponse(
    val roomId: String?,
    val roomName: String?,
    val owner: String?,
    val channelName: String?,
    val participants: List<String> = emptyList(),
    val participantInfos: List<RoomParticipant> = emptyList(),
    val pendingInvitations: List<String> = emptyList()
)

/**
 * 语聊房消息响应数据类。
 */
data class RoomMessageResponse(
    val sender: String?,
    val content: String?,
    val timestamp: Long = 0
)

/**
 * SoulApiService 是 SOUL_Android 应用的核心网络接口服务。
 * 提供用户认证、个人资料、匹配推荐、即时聊天、音视频通话、好友关系、语聊房以及 AI 智能助手等全部 RESTful API 端点。
 */
interface SoulApiService {

    // ---- 注册查重 / 通用上传 ----

    /**
     * 检查用户名是否已被注册（查重）。
     * 对应后端端点：GET /api/register/check
     *
     * @param username 要检查的用户名
     * @return 包含可用性状态的响应包装对象 [Response]<[UsernameCheckResponse]>
     */
    @GET("api/register/check")
    suspend fun checkUsername(
        @Query("username") username: String
    ): Response<UsernameCheckResponse>

    /**
     * 上传通用文件到服务器。
     * 对应后端端点：POST /api/upload
     *
     * @param file 多部分文件对象 [MultipartBody.Part]
     * @return 包含文件访问 URL 的响应包装对象 [Response]<[UploadUrlResponse]>
     */
    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadUrlResponse>



    // =====================================================
    // Login
    // =====================================================

    /**
     * 用户登录验证。
     * 对应后端端点：POST /api/login
     *
     * @param request 登录请求体 [LoginRequest]，包含用户名和密码
     * @return 登录结果响应包装对象 [Response]<[LoginResponse]>
     */
    @POST("api/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * 新用户注册。
     * 对应后端端点：POST /api/register
     *
     * @param request 注册请求体 [RegisterRequest]，包含注册所需详细信息
     * @return 注册结果响应包装对象 [Response]<[LoginResponse]>
     */
    @POST("api/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<LoginResponse>


    // =====================================================
    // Profile
    // =====================================================

    /**
     * 获取指定用户的个人详细资料。
     * 对应后端端点：GET /api/user/profile
     *
     * @param username 目标用户的用户名
     * @return 用户资料响应包装对象 [Response]<[ProfileResponse]>
     */
    @GET("api/user/profile")
    suspend fun getUserProfile(
        @Query("username")
        username: String
    ): Response<ProfileResponse>

    /**
     * 更新当前用户的个人资料。
     * 对应后端端点：PUT /api/user/profile
     *
     * @param profile 更新后的用户资料对象 [ProfileResponse]
     * @return 更新后的用户资料响应包装对象 [Response]<[ProfileResponse]>
     */
    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Body
        profile: ProfileResponse
    ): Response<ProfileResponse>


    // =====================================================
    // Avatar
    // =====================================================

    /**
     * 上传用户头像。
     * 对应后端端点：POST /api/user/avatar
     *
     * @param username 用户名请求体 [RequestBody]
     * @param file 头像图片文件 [MultipartBody.Part]
     * @return 头像上传结果响应包装对象 [Response]<[AvatarUploadResponse]>
     */
    @Multipart
    @POST("api/user/avatar")
    suspend fun uploadAvatar(
        @Part("username")
        username: RequestBody,
        @Part
        file: MultipartBody.Part
    ): Response<AvatarUploadResponse>


    // =====================================================
    // Match profile
    // =====================================================

    /**
     * 获取指定用户的匹配档案详情（含历史匹配）。
     * 对应后端端点：GET /api/match/profile/{userId}
     *
     * @param userId 用户的唯一数字ID
     * @return 用户匹配档案对象 [UserMatchProfileResponse]
     */
    @GET("api/match/profile/{userId}")
    suspend fun getUserMatchProfile(
        @Path("userId")
        userId: Long
    ): UserMatchProfileResponse


    // =====================================================
    // Match
    // =====================================================

    /**
     * 根据筛选条件获取推荐匹配用户列表。
     * 对应后端端点：GET /api/match
     *
     * @param username 当前用户名
     * @param haveSkill 持有技能筛选（可选）
     * @param wantSkill 期望技能筛选（可选）
     * @param timeSlot 可用时间段筛选（可选）
     * @param skillWantLevel 期望技能等级筛选（可选）
     * @param skillOfferLevel 持有技能等级筛选（可选）
     * @param limit 返回数量限制（默认 12）
     * @return 匹配用户响应列表 [List]<[MatchUserResponse]>
     */
    @GET("api/match")
    suspend fun getMatches(
        @Query("username")
        username: String,
        @Query("haveSkill")
        haveSkill: String? = null,
        @Query("wantSkill")
        wantSkill: String? = null,
        @Query("timeSlot")
        timeSlot: String? = null,
        @Query("skillWantLevel")
        skillWantLevel: String? = null,
        @Query("skillOfferLevel")
        skillOfferLevel: String? = null,
        @Query("limit")
        limit: Int = 12
    ): List<MatchUserResponse>

    /**
     * 获取当前用户已建立的匹配用户列表。
     * 对应后端端点：GET /api/match/mine
     *
     * @param username 当前用户名
     * @return 匹配用户响应列表 [List]<[MatchUserResponse]>
     */
    @GET("api/match/mine")
    suspend fun getMyMatches(
        @Query("username")
        username: String
    ): List<MatchUserResponse>

    /**
     * 全局搜索用户。
     * 对应后端端点：GET /api/search/users
     *
     * @param username 当前用户名
     * @param q 搜索关键词
     * @param limit 返回数量限制（默认 50）
     * @return 搜索到的用户响应列表 [List]<[MatchUserResponse]>
     */
    @GET("api/search/users")
    suspend fun searchUsers(
        @Query("username")
        username: String,
        @Query("q")
        q: String,
        @Query("limit")
        limit: Int = 50
    ): List<MatchUserResponse>


    // =====================================================
    // Chat
    // =====================================================

    /**
     * 获取当前用户参与的所有聊天会话房间列表。
     * 对应后端端点：GET /api/chat/rooms
     *
     * @param username 当前用户名
     * @return 聊天会话列表 [List]<[Map]<[String], [Any]>>
     */
    @GET("api/chat/rooms")
    suspend fun getChatRooms(
        @Query("username")
        username: String
    ): List<Map<String, Any>>

    /**
     * 创建或获取与指定用户的私聊会话。
     * 对应后端端点：POST /api/chat/direct
     *
     * @param request 聊天发送/创建请求体 [ChatSendRequest]
     * @return 会话详情响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/chat/direct")
    suspend fun createDirectChatRoom(
        @Body
        request: ChatSendRequest
    ): Response<Map<String, Any>>

    /**
     * 获取与指定聊天伙伴的历史聊天消息记录。
     * 对应后端端点：GET /api/chat/messages
     *
     * @param me 当前用户用户名
     * @param partner 聊天伙伴用户名
     * @return 后端聊天消息列表 [List]<[BackendChatMessage]>
     */
    @GET("api/chat/messages")
    suspend fun getChatMessages(
        @Query("me")
        me: String,
        @Query("partner")
        partner: String
    ): List<BackendChatMessage>

    /**
     * 发送文本或其他类型的聊天消息。
     * 对应后端端点：POST /api/chat/send
     *
     * @param request 聊天消息请求体 [ChatSendRequest]
     * @return 发送结果响应包装对象 [Response]<[ChatSendResponse]>
     */
    @POST("api/chat/send")
    suspend fun sendMessage(
        @Body
        request: ChatSendRequest
    ): Response<ChatSendResponse>

    /**
     * 发送图片聊天消息。
     * 对应后端端点：POST /api/chat/image
     *
     * @param senderUsername 发送者用户名请求体 [RequestBody]
     * @param receiverUsername 接收者用户名请求体 [RequestBody]
     * @param file 图片文件 [MultipartBody.Part]
     * @return 发送结果响应包装对象 [Response]<[ChatSendResponse]>
     */
    @Multipart
    @POST("api/chat/image")
    suspend fun sendImage(
        @Part("senderUsername") senderUsername: RequestBody,
        @Part("receiverUsername") receiverUsername: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ChatSendResponse>

    /**
     * 撤回已发送的聊天消息。
     * 对应后端端点：POST /api/chat/recall
     *
     * @param request 消息操作请求体 [MessageActionRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/chat/recall")
    suspend fun recallMessage(
        @Body request: MessageActionRequest
    ): Response<Map<String, Any>>

    /**
     * 删除指定的聊天消息。
     * 对应后端端点：POST /api/chat/delete
     *
     * @param request 消息操作请求体 [MessageActionRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/chat/delete")
    suspend fun deleteMessage(
        @Body request: MessageActionRequest
    ): Response<Map<String, Any>>

    /**
     * 标记与某用户的聊天消息为已读状态。
     * 对应后端端点：POST /api/chat/read
     *
     * @param request 已读标记请求体 [ReadRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/chat/read")
    suspend fun markRead(
        @Body request: ReadRequest
    ): Response<Map<String, Any>>

    /**
     * 获取未读消息统计或未读列表。
     * 对应后端端点：GET /api/chat/unread
     *
     * @param username 当前用户名
     * @return 未读消息信息列表 [List]<[Map]<[String], [Any]>>
     */
    @GET("api/chat/unread")
    suspend fun getUnread(
        @Query("username") username: String
    ): List<Map<String, Any>>


    // =====================================================
    // Call
    // =====================================================

    /**
     * 发起音视频通话请求。
     * 对应后端端点：POST /api/call/start
     *
     * @param request 通话发起请求体 [CallStartRequest]
     * @return 通话状态响应对象 [CallStatusResponse]
     */
    @POST("api/call/start")
    suspend fun startCall(
        @Body request: CallStartRequest
    ): CallStatusResponse

    /**
     * 接听或拒绝音视频通话。
     * 对应后端端点：POST /api/call/answer
     *
     * @param request 通话应答请求体 [CallAnswerRequest]
     * @return 通话状态响应对象 [CallStatusResponse]
     */
    @POST("api/call/answer")
    suspend fun answerCall(
        @Body request: CallAnswerRequest
    ): CallStatusResponse

    /**
     * 结束正在进行或振铃中的音视频通话。
     * 对应后端端点：POST /api/call/end
     *
     * @param request 通话结束请求体 [CallEndRequest]
     * @return 通话状态响应对象 [CallStatusResponse]
     */
    @POST("api/call/end")
    suspend fun endCall(
        @Body request: CallEndRequest
    ): CallStatusResponse

    /**
     * 检查是否有呼入的音视频通话。
     * 对应后端端点：GET /api/call/incoming
     *
     * @param username 当前用户名
     * @return 通话状态响应对象 [CallStatusResponse]
     */
    @GET("api/call/incoming")
    suspend fun incomingCall(
        @Query("username") username: String
    ): CallStatusResponse

    /**
     * 查询指定通话 ID 的当前状态。
     * 对应后端端点：GET /api/call/status
     *
     * @param callId 通话唯一标识符
     * @return 通话状态响应对象 [CallStatusResponse]
     */
    @GET("api/call/status")
    suspend fun callStatus(
        @Query("callId") callId: String
    ): CallStatusResponse

    /**
     * 获取声网（Agora）音视频通话的 Channel Token 和 UID。
     * 对应后端端点：GET /api/call/token
     *
     * @param channelName 频道名称
     * @param uid 用户数字 ID（默认 0）
     * @return 声网 Token 响应对象 [CallTokenResponse]
     */
    @GET("api/call/token")
    suspend fun getCallToken(
        @Query("channelName") channelName: String,
        @Query("uid") uid: Int = 0
    ): CallTokenResponse


    // =====================================================
    // Friend / Block
    // =====================================================

    /**
     * 查询与指定用户的关系状态（匹配、好友、拉黑等）。
     * 对应后端端点：GET /api/relation/status
     *
     * @param username 当前用户名
     * @param other 对方用户名
     * @return 关系状态对象 [RelationStatus]
     */
    @GET("api/relation/status")
    suspend fun relationStatus(
        @Query("username") username: String,
        @Query("other") other: String
    ): RelationStatus

    /**
     * 发送好友申请。
     * 对应后端端点：POST /api/friend/request
     *
     * @param request 好友请求体 [FriendRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/friend/request")
    suspend fun requestFriend(
        @Body request: FriendRequest
    ): Response<Map<String, Any>>

    /**
     * 接受或拒绝好友申请。
     * 对应后端端点：POST /api/friend/respond
     *
     * @param request 好友响应请求体 [FriendRespondRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/friend/respond")
    suspend fun respondFriendRequest(
        @Body request: FriendRespondRequest
    ): Response<Map<String, Any>>

    /**
     * 获取收到的所有待处理好友申请列表。
     * 对应后端端点：GET /api/friend/requests
     *
     * @param username 当前用户名
     * @return 好友申请记录响应列表 [List]<[FriendRequestResponse]>
     */
    @GET("api/friend/requests")
    suspend fun listFriendRequests(
        @Query("username") username: String
    ): List<FriendRequestResponse>

    /**
     * 删除好友。
     * 对应后端端点：POST /api/friend/remove
     *
     * @param request 好友删除请求体 [FriendRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/friend/remove")
    suspend fun removeFriend(
        @Body request: FriendRequest
    ): Response<Map<String, Any>>

    /**
     * 获取当前用户的好友列表。
     * 对应后端端点：GET /api/friend/list
     *
     * @param username 当前用户名
     * @return 好友用户响应列表（以匹配用户格式返回） [List]<[MatchUserResponse]>
     */
    @GET("api/friend/list")
    suspend fun listFriends(
        @Query("username") username: String
    ): List<MatchUserResponse>

    /**
     * 拉黑指定用户。
     * 对应后端端点：POST /api/block/add
     *
     * @param request 拉黑请求体 [BlockRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/block/add")
    suspend fun addBlock(
        @Body request: BlockRequest
    ): Response<Map<String, Any>>

    /**
     * 取消拉黑指定用户。
     * 对应后端端点：POST /api/block/remove
     *
     * @param request 取消拉黑请求体 [BlockRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/block/remove")
    suspend fun removeBlock(
        @Body request: BlockRequest
    ): Response<Map<String, Any>>

    /**
     * 获取当前用户的黑名单用户列表。
     * 对应后端端点：GET /api/block/list
     *
     * @param username 当前用户名
     * @return 黑名单用户响应列表 [List]<[MatchUserResponse]>
     */
    @GET("api/block/list")
    suspend fun listBlocks(
        @Query("username") username: String
    ): List<MatchUserResponse>

    /**
     * 设置对指定用户的备注名称。
     * 对应后端端点：POST /api/remark/set
     *
     * @param request 备注设置请求体 [RemarkRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/remark/set")
    suspend fun setRemark(
        @Body request: RemarkRequest
    ): Response<Map<String, Any>>


    // =====================================================
    // Room（语聊房）
    // =====================================================

    /**
     * 创建语聊房。
     * 对应后端端点：POST /api/room/create
     *
     * @param request 语聊房创建请求体 [RoomCreateRequest]
     * @return 语聊房状态响应对象 [RoomStatusResponse]
     */
    @POST("api/room/create")
    suspend fun createRoom(
        @Body request: RoomCreateRequest
    ): RoomStatusResponse

    /**
     * 邀请其他用户加入语聊房。
     * 对应后端端点：POST /api/room/invite
     *
     * @param request 语聊房邀请请求体 [RoomInviteRequest]
     * @return 语聊房状态响应对象 [RoomStatusResponse]
     */
    @POST("api/room/invite")
    suspend fun inviteToRoom(
        @Body request: RoomInviteRequest
    ): RoomStatusResponse

    /**
     * 接受或拒绝语聊房邀请。
     * 对应后端端点：POST /api/room/respond
     *
     * @param request 语聊房邀请响应请求体 [RoomRespondRequest]
     * @return 语聊房状态响应对象 [RoomStatusResponse]
     */
    @POST("api/room/respond")
    suspend fun respondToInvite(
        @Body request: RoomRespondRequest
    ): RoomStatusResponse

    /**
     * 查询语聊房的当前状态与参与者列表。
     * 对应后端端点：GET /api/room/status
     *
     * @param roomId 语聊房唯一标识符
     * @return 语聊房状态响应对象 [RoomStatusResponse]
     */
    @GET("api/room/status")
    suspend fun roomStatus(
        @Query("roomId") roomId: String
    ): RoomStatusResponse

    /**
     * 获取当前用户收到的所有语聊房邀请列表。
     * 对应后端端点：GET /api/room/incoming
     *
     * @param username 当前用户名
     * @return 语聊房状态响应列表 [List]<[RoomStatusResponse]>
     */
    @GET("api/room/incoming")
    suspend fun incomingRooms(
        @Query("username") username: String
    ): List<RoomStatusResponse>

    /**
     * 离开语聊房。
     * 对应后端端点：POST /api/room/leave
     *
     * @param request 离开请求体 [RoomRespondRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/room/leave")
    suspend fun leaveRoom(
        @Body request: RoomRespondRequest
    ): Response<Map<String, Any>>

    /**
     * 在语聊房内发送文本广播消息。
     * 对应后端端点：POST /api/room/message
     *
     * @param request 语聊房消息请求体 [RoomMessageRequest]
     * @return 操作结果响应包装对象 [Response]<[Map]<[String], [Any]>>
     */
    @POST("api/room/message")
    suspend fun sendRoomMessage(
        @Body request: RoomMessageRequest
    ): Response<Map<String, Any>>

    /**
     * 获取语聊房内的历史聊天或通知消息列表。
     * 对应后端端点：GET /api/room/messages
     *
     * @param roomId 语聊房唯一标识符
     * @return 语聊房消息响应列表 [List]<[RoomMessageResponse]>
     */
    @GET("api/room/messages")
    suspend fun roomMessages(
        @Query("roomId") roomId: String
    ): List<RoomMessageResponse>


    // =====================================================
    // AI
    // =====================================================

    /**
     * 获取 AI 匹配指南或智能问答建议。
     * 对应后端端点：GET /api/ai/guide
     *
     * @param username 当前用户名
     * @param targetLang 目标语言
     * @return AI 指引响应包装对象 [Response]<[AiGuideResponse]>
     */
    @GET("api/ai/guide")
    suspend fun getAiGuide(
        @Query("username")
        username: String,
        @Query("targetLang")
        targetLang: String
    ): Response<AiGuideResponse>

    /**
     * 使用 AI 翻译服务翻译指定文本。
     * 对应后端端点：POST /api/ai/translate
     *
     * @param text 要翻译的文本内容
     * @param targetLang 目标语言代码
     * @return AI 翻译结果响应包装对象 [Response]<[AiGuideResponse]>
     */
    @POST("api/ai/translate")
    suspend fun translateText(
        @Query("text")
        text: String,
        @Query("targetLang")
        targetLang: String
    ): Response<AiGuideResponse>


    companion object {

        /**
         * 获取当前 API 服务的基础请求地址（Base URL）。
         * 会动态读取 [BuildConfig.API_BASE_URL]（可在登录页或设置中修改）。
         */
        val BASE_URL: String
            get() = BuildConfig.API_BASE_URL

        /**
         * 创建并返回一个配置好的 [SoulApiService] 实例。
         * 内置日志拦截器 [HttpLoggingInterceptor]、超时设置（连接、读取、写入各 15 秒）以及 Gson 转换工厂。
         *
         * @return [SoulApiService] 实例
         */
        fun create(): SoulApiService {
            val logger =
                HttpLoggingInterceptor()
                    .apply {
                        level =
                            if (BuildConfig.DEBUG)
                                HttpLoggingInterceptor
                                    .Level
                                    .BODY
                            else
                                HttpLoggingInterceptor
                                    .Level
                                    .NONE
                    }

            val client =
                OkHttpClient
                    .Builder()
                    .addInterceptor(logger)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()

            return Retrofit
                .Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory
                        .create()
                )
                .build()
                .create(
                    SoulApiService::class.java
                )
        }
    }
}
