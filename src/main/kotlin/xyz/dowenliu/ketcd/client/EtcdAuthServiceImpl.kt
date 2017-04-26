package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.ByteString
import io.grpc.ManagedChannel
import xyz.dowenliu.ketcd.UsernamePassword
import xyz.dowenliu.ketcd.api.*
import xyz.dowenliu.ketcd.protobuf.toByteString

/**
 * Implementation of etcd auth service.
 *
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */
class EtcdAuthServiceImpl internal constructor(override val client: EtcdClient) : EtcdAuthService {
    private val channel: ManagedChannel = client.channelBuilder.build()
    private val blockingStub = configureStub(AuthGrpc.newBlockingStub(channel), client.token)
    private val futureStub = configureStub(AuthGrpc.newFutureStub(channel), client.token)
    private val asyncStub = configureStub(AuthGrpc.newStub(channel), client.token)

    override fun authEnable(): AuthEnableResponse = blockingStub.authEnable(AuthEnableRequest.getDefaultInstance())

    override fun authEnableInFuture(): ListenableFuture<AuthEnableResponse> =
            futureStub.authEnable(AuthEnableRequest.getDefaultInstance())

    override fun authEnableAsync(callback: ResponseCallback<AuthEnableResponse>) =
            asyncStub.authEnable(AuthEnableRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    override fun authDisable(): AuthDisableResponse = blockingStub.authDisable(AuthDisableRequest.getDefaultInstance())

    override fun authDisableInFuture(): ListenableFuture<AuthDisableResponse> =
            futureStub.authDisable(AuthDisableRequest.getDefaultInstance())

    override fun authDisableAsync(callback: ResponseCallback<AuthDisableResponse>) =
            asyncStub.authDisable(AuthDisableRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    override fun userList(): AuthUserListResponse = blockingStub.userList(AuthUserListRequest.getDefaultInstance())

    override fun userListInFuture(): ListenableFuture<AuthUserListResponse> =
            futureStub.userList(AuthUserListRequest.getDefaultInstance())

    override fun userListAsync(callback: ResponseCallback<AuthUserListResponse>) =
            asyncStub.userList(AuthUserListRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    private fun userGetRequest(username: String): AuthUserGetRequest =
            AuthUserGetRequest.newBuilder().setNameBytes(username.toByteString()).build()

    override fun userGet(username: String): AuthUserGetResponse = blockingStub.userGet(userGetRequest(username))

    override fun userGetInFuture(username: String): ListenableFuture<AuthUserGetResponse> =
            futureStub.userGet(userGetRequest(username))

    override fun userGetAsync(username: String, callback: ResponseCallback<AuthUserGetResponse>) =
            asyncStub.userGet(userGetRequest(username), CallbackStreamObserver(callback))

    private fun userAddRequest(usernamePassword: UsernamePassword): AuthUserAddRequest =
            AuthUserAddRequest.newBuilder()
                    .setNameBytes(usernamePassword.username)
                    .setPasswordBytes(usernamePassword.password)
                    .build()

    override fun userAdd(usernamePassword: UsernamePassword): AuthUserAddResponse =
            blockingStub.userAdd(userAddRequest(usernamePassword))

    override fun userAddInFuture(usernamePassword: UsernamePassword): ListenableFuture<AuthUserAddResponse> =
            futureStub.userAdd(userAddRequest(usernamePassword))

    override fun userAddAsync(usernamePassword: UsernamePassword, callback: ResponseCallback<AuthUserAddResponse>) =
            asyncStub.userAdd(userAddRequest(usernamePassword), CallbackStreamObserver(callback))

    private fun userChangePasswordRequest(usernamePassword: UsernamePassword): AuthUserChangePasswordRequest =
            AuthUserChangePasswordRequest.newBuilder()
                    .setNameBytes(usernamePassword.username)
                    .setPasswordBytes(usernamePassword.password)
                    .build()

    override fun userChangePassword(usernamePassword: UsernamePassword): AuthUserChangePasswordResponse =
            blockingStub.userChangePassword(userChangePasswordRequest(usernamePassword))

    override fun userChangePasswordInFuture(usernamePassword: UsernamePassword):
            ListenableFuture<AuthUserChangePasswordResponse> =
            futureStub.userChangePassword(userChangePasswordRequest(usernamePassword))

    override fun userChangePasswordAsync(usernamePassword: UsernamePassword,
                                         callback: ResponseCallback<AuthUserChangePasswordResponse>) =
            asyncStub.userChangePassword(userChangePasswordRequest(usernamePassword), CallbackStreamObserver(callback))

    private fun userDeleteRequest(username: String): AuthUserDeleteRequest =
            AuthUserDeleteRequest.newBuilder().setNameBytes(username.toByteString()).build()

    override fun userDelete(username: String): AuthUserDeleteResponse =
            blockingStub.userDelete(userDeleteRequest(username))

    override fun userDeleteInFuture(username: String): ListenableFuture<AuthUserDeleteResponse> =
            futureStub.userDelete(userDeleteRequest(username))

    override fun userDeleteAsync(username: String, callback: ResponseCallback<AuthUserDeleteResponse>) =
            asyncStub.userDelete(userDeleteRequest(username), CallbackStreamObserver(callback))

    override fun roleList(): AuthRoleListResponse = blockingStub.roleList(AuthRoleListRequest.getDefaultInstance())

    override fun roleListInFuture(): ListenableFuture<AuthRoleListResponse> =
            futureStub.roleList(AuthRoleListRequest.getDefaultInstance())

    override fun roleListAsync(callback: ResponseCallback<AuthRoleListResponse>) =
            asyncStub.roleList(AuthRoleListRequest.getDefaultInstance(), CallbackStreamObserver(callback))

    private fun roleGetRequest(role: String): AuthRoleGetRequest =
            AuthRoleGetRequest.newBuilder().setRoleBytes(role.toByteString()).build()

    override fun roleGet(role: String): AuthRoleGetResponse = blockingStub.roleGet(roleGetRequest(role))

    override fun roleGetInFuture(role: String): ListenableFuture<AuthRoleGetResponse> =
            futureStub.roleGet(roleGetRequest(role))

    override fun roleGetAsync(role: String, callback: ResponseCallback<AuthRoleGetResponse>) =
            asyncStub.roleGet(roleGetRequest(role), CallbackStreamObserver(callback))

    private fun roleAddRequest(name: String): AuthRoleAddRequest =
            AuthRoleAddRequest.newBuilder().setNameBytes(name.toByteString()).build()

    override fun roleAdd(name: String): AuthRoleAddResponse = blockingStub.roleAdd(roleAddRequest(name))

    override fun roleAddInFuture(name: String): ListenableFuture<AuthRoleAddResponse> =
            futureStub.roleAdd(roleAddRequest(name))

    override fun roleAddAsync(name: String, callback: ResponseCallback<AuthRoleAddResponse>) =
            asyncStub.roleAdd(roleAddRequest(name), CallbackStreamObserver(callback))

    private fun roleGrantPermissionRequest(role: String,
                                           permType: Permission.Type,
                                           key: ByteString,
                                           rangeEnd: ByteString): AuthRoleGrantPermissionRequest {
        val permission = Permission.newBuilder()
                .setKey(key)
                .setRangeEnd(rangeEnd)
                .setPermType(permType)
                .build()
        return AuthRoleGrantPermissionRequest.newBuilder()
                .setNameBytes(role.toByteString())
                .setPerm(permission)
                .build()
    }

    override fun roleGrantPermission(role: String,
                                     permType: Permission.Type,
                                     key: ByteString,
                                     rangeEnd: ByteString): AuthRoleGrantPermissionResponse =
            blockingStub.roleGrantPermission(roleGrantPermissionRequest(role, permType, key, rangeEnd))

    override fun roleGrantPermissionInFuture(role: String,
                                             permType: Permission.Type,
                                             key: ByteString,
                                             rangeEnd: ByteString): ListenableFuture<AuthRoleGrantPermissionResponse> =
            futureStub.roleGrantPermission(roleGrantPermissionRequest(role, permType, key, rangeEnd))

    override fun roleGrantPermissionAsync(role: String,
                                          permType: Permission.Type,
                                          key: ByteString,
                                          rangeEnd: ByteString,
                                          callback: ResponseCallback<AuthRoleGrantPermissionResponse>) =
            asyncStub.roleGrantPermission(roleGrantPermissionRequest(role, permType, key, rangeEnd),
                    CallbackStreamObserver(callback))

    private fun roleRevokePermissionRequest(role: String,
                                            key: ByteString,
                                            rangeEnd: ByteString): AuthRoleRevokePermissionRequest =
            AuthRoleRevokePermissionRequest.newBuilder()
                    .setRoleBytes(role.toByteString())
                    .setKeyBytes(key)
                    .setRoleBytes(rangeEnd)
                    .build()

    override fun roleRevokePermission(role: String, key: ByteString,
                                      rangeEnd: ByteString): AuthRoleRevokePermissionResponse =
            blockingStub.roleRevokePermission(roleRevokePermissionRequest(role, key, rangeEnd))

    override fun roleRevokePermissionInFuture(role: String,
                                              key: ByteString,
                                              rangeEnd: ByteString):
            ListenableFuture<AuthRoleRevokePermissionResponse> =
            futureStub.roleRevokePermission(roleRevokePermissionRequest(role, key, rangeEnd))

    override fun roleRevokePermissionAsync(role: String,
                                           key: ByteString,
                                           rangeEnd: ByteString,
                                           callback: ResponseCallback<AuthRoleRevokePermissionResponse>) =
            asyncStub.roleRevokePermission(roleRevokePermissionRequest(role, key, rangeEnd),
                    CallbackStreamObserver(callback))

    private fun roleDeleteRequest(role: String): AuthRoleDeleteRequest =
            AuthRoleDeleteRequest.newBuilder().setRoleBytes(role.toByteString()).build()

    override fun roleDelete(role: String): AuthRoleDeleteResponse = blockingStub.roleDelete(roleDeleteRequest(role))

    override fun roleDeleteInFuture(role: String): ListenableFuture<AuthRoleDeleteResponse> =
            futureStub.roleDelete(roleDeleteRequest(role))

    override fun roleDeleteAsync(role: String, callback: ResponseCallback<AuthRoleDeleteResponse>) =
            asyncStub.roleDelete(roleDeleteRequest(role), CallbackStreamObserver(callback))

    private fun userGrantRoleRequest(username: String, role: String): AuthUserGrantRoleRequest =
            AuthUserGrantRoleRequest.newBuilder()
                    .setUserBytes(username.toByteString())
                    .setRoleBytes(role.toByteString())
                    .build()

    override fun userGrantRole(username: String, role: String): AuthUserGrantRoleResponse =
            blockingStub.userGrantRole(userGrantRoleRequest(username, role))

    override fun userGrantRoleInFuture(username: String, role: String): ListenableFuture<AuthUserGrantRoleResponse> =
            futureStub.userGrantRole(userGrantRoleRequest(username, role))

    override fun userGrantRoleAsync(username: String, role: String,
                                    callback: ResponseCallback<AuthUserGrantRoleResponse>) =
            asyncStub.userGrantRole(userGrantRoleRequest(username, role), CallbackStreamObserver(callback))

    private fun userRevokeRoleRequest(username: String, role: String): AuthUserRevokeRoleRequest =
            AuthUserRevokeRoleRequest.newBuilder()
                    .setNameBytes(username.toByteString())
                    .setRoleBytes(role.toByteString())
                    .build()

    override fun userRevokeRole(username: String, role: String): AuthUserRevokeRoleResponse =
            blockingStub.userRevokeRole(userRevokeRoleRequest(username, role))

    override fun userRevokeRoleInFuture(username: String, role: String): ListenableFuture<AuthUserRevokeRoleResponse> =
            futureStub.userRevokeRole(userRevokeRoleRequest(username, role))

    override fun userRevokeRoleAsync(username: String, role: String,
                                     callback: ResponseCallback<AuthUserRevokeRoleResponse>) =
            asyncStub.userRevokeRole(userRevokeRoleRequest(username, role), CallbackStreamObserver(callback))
}