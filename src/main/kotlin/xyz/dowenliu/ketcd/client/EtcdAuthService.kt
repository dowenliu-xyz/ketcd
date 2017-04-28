package xyz.dowenliu.ketcd.client

import com.google.common.util.concurrent.ListenableFuture
import com.google.protobuf.ByteString
import xyz.dowenliu.ketcd.UsernamePassword
import xyz.dowenliu.ketcd.api.*

/**
 * Interface of auth service talking to etcd
 *
 * create at 2017/4/17
 * @author liufl
 * @since 0.1.0
 */
interface EtcdAuthService {
    /**
     * The client which created this service.
     */
    val client: EtcdClient

    /**
     * Enable authentication (blocking).
     *
     * @return [AuthEnableResponse]
     */
    fun authEnable(): AuthEnableResponse

    /**
     * Enables authentication in future.
     *
     * @return [ListenableFuture] of [AuthEnableResponse]
     */
    fun authEnableInFuture(): ListenableFuture<AuthEnableResponse>

    /**
     * Enables authentication (asynchronously).
     *
     * @param callback A [ResponseCallback] to handle received response.
     */
    fun authEnableAsync(callback: ResponseCallback<AuthEnableResponse>)

    /**
     * Disables authentication (blocking).
     *
     * @return [AuthDisableResponse]
     */
    fun authDisable(): AuthDisableResponse

    /**
     * Disable authentication in future.
     *
     * @return [ListenableFuture] of [AuthDisableResponse]
     */
    fun authDisableInFuture(): ListenableFuture<AuthDisableResponse>

    /**
     * Disable authentication (asynchronously).
     *
     * @param callback A [ResponseCallback] to handle received response.
     */
    fun authDisableAsync(callback: ResponseCallback<AuthDisableResponse>)

    /**
     * Gets a list of all users (blocking).
     *
     * @return [AuthUserListResponse]
     */
    fun userList(): AuthUserListResponse

    /**
     * Gets a list of all users in future.
     *
     * @return [ListenableFuture] of [AuthUserListResponse]
     */
    fun userListInFuture(): ListenableFuture<AuthUserListResponse>

    /**
     * Gets a list of all users (asynchronously).
     *
     * @param callback A [ResponseCallback] to handle received response.
     */
    fun userListAsync(callback: ResponseCallback<AuthUserListResponse>)

    /**
     * Gets detailed user information (blocking).
     *
     * @param username the username of the user to get.
     * @return [AuthUserGetResponse]
     */
    fun userGet(username: String): AuthUserGetResponse

    /**
     * Gets detailed user information in future.
     *
     * @param username The username of the user to get.
     * @return [ListenableFuture] of [AuthUserGetResponse]
     */
    fun userGetInFuture(username: String): ListenableFuture<AuthUserGetResponse>

    /**
     * Gets detailed user information (asynchronously).
     *
     * @param username The username of the user to get.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userGetAsync(username: String, callback: ResponseCallback<AuthUserGetResponse>)

    /**
     * Adds a new user (blocking).
     *
     * @param usernamePassword The username and password info of the new user
     * @return [AuthUserAddResponse]
     */
    fun userAdd(usernamePassword: UsernamePassword): AuthUserAddResponse

    /**
     * Adds a new user in future.
     *
     * @param usernamePassword The username and password info of the new user.
     * @return [ListenableFuture] of [AuthUserAddResponse]
     */
    fun userAddInFuture(usernamePassword: UsernamePassword): ListenableFuture<AuthUserAddResponse>

    /**
     * Adds a new user (asynchronously).
     *
     * @param usernamePassword The username and password info of the new user.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userAddAsync(usernamePassword: UsernamePassword, callback: ResponseCallback<AuthUserAddResponse>)

    /**
     * Changes the password of a specified user (blocking).
     *
     * @param usernamePassword The username of the user and the new password to change to
     * @return [AuthUserChangePasswordResponse]
     */
    fun userChangePassword(usernamePassword: UsernamePassword): AuthUserChangePasswordResponse

    /**
     * Changes the password of a specified user in future.
     *
     * @param usernamePassword The username of the user and the new password to change to
     * @return [ListenableFuture] of [AuthUserChangePasswordResponse]
     */
    fun userChangePasswordInFuture(usernamePassword: UsernamePassword): ListenableFuture<AuthUserChangePasswordResponse>

    /**
     * Changes the password of a specified user (asynchronously).
     *
     * @param usernamePassword The username of the user and the new password to change to
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userChangePasswordAsync(usernamePassword: UsernamePassword,
                                callback: ResponseCallback<AuthUserChangePasswordResponse>)

    /**
     * Deletes a specified user (blocking).
     *
     * @param username the username of the user to delete
     * @return [AuthUserDeleteResponse]
     */
    fun userDelete(username: String): AuthUserDeleteResponse

    /**
     * Deletes a specified user in future.
     *
     * @param username the username of the user to delete
     * @return [ListenableFuture] of [AuthUserDeleteResponse]
     */
    fun userDeleteInFuture(username: String): ListenableFuture<AuthUserDeleteResponse>

    /**
     * Deletes a specified user (asynchronously).
     *
     * @param username the username of the user to delete
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userDeleteAsync(username: String, callback: ResponseCallback<AuthUserDeleteResponse>)

    /**
     * Gets a list of all roles (blocking).
     *
     * @return [AuthRoleListResponse]
     */
    fun roleList(): AuthRoleListResponse

    /**
     * Gets a list of all roles in future.
     *
     * @return [ListenableFuture] of [AuthRoleListResponse]
     */
    fun roleListInFuture(): ListenableFuture<AuthRoleListResponse>

    /**
     * Gets a list of all roles (asynchronously).
     *
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun roleListAsync(callback: ResponseCallback<AuthRoleListResponse>)

    /**
     * Gets detailed role information (blocking).
     *
     * @param role The name of the role to get.
     * @return [AuthRoleGetResponse]
     */
    fun roleGet(role: String): AuthRoleGetResponse

    /**
     * Gets detailed role information in future.
     *
     * @param role The name of the role to get.
     * @return [ListenableFuture] of [AuthRoleGetResponse]
     */
    fun roleGetInFuture(role: String): ListenableFuture<AuthRoleGetResponse>

    /**
     * Gets detailed role information (asynchronously).
     *
     * @param role The name of the role to get.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun roleGetAsync(role: String, callback: ResponseCallback<AuthRoleGetResponse>)

    /**
     * Adds a new role (blocking).
     *
     * @param name The name of the new role.
     * @return [AuthRoleAddResponse]
     */
    fun roleAdd(name: String): AuthRoleAddResponse

    /**
     * Adds a new role in future.
     *
     * @param name The name of the new role.
     * @return [ListenableFuture] of [AuthRoleAddResponse]
     */
    fun roleAddInFuture(name: String): ListenableFuture<AuthRoleAddResponse>

    /**
     * Adds a new role (asynchronously).
     *
     * @param name The name of the new role.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun roleAddAsync(name: String, callback: ResponseCallback<AuthRoleAddResponse>)

    /**
     * Grants a permission of a specified key or range to a specified role (blocking).
     *
     * @param role the name of the role to grant to.
     * @param permType the permission type to grant.
     * @param key the target key to grant on.
     * @param rangeEnd the target range to grant on to. [ByteString.EMPTY] means just key, no range.
     * @return [AuthRoleGrantPermissionResponse]
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleGrantPermission(role: String,
                            permType: Permission.Type,
                            key: ByteString,
                            rangeEnd: ByteString = ByteString.EMPTY): AuthRoleGrantPermissionResponse

    /**
     * Grants a permission of a specified key or range to a specified role in future.
     *
     * @param role the name of the role to grant to.
     * @param permType the permission type to grant.
     * @param key the target key to grant on.
     * @param rangeEnd the target range to grant on to. [ByteString.EMPTY] means just key, no range.
     * @return [ListenableFuture] of [AuthRoleGrantPermissionResponse]
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleGrantPermissionInFuture(role: String,
                                    permType: Permission.Type,
                                    key: ByteString,
                                    rangeEnd: ByteString = ByteString.EMPTY):
            ListenableFuture<AuthRoleGrantPermissionResponse>

    /**
     * Grants a permission of a specified key or range to a specified role (asynchronously).
     *
     * @param role the name of the role to grant to.
     * @param permType the permission type to grant.
     * @param key the target key to grant on.
     * @param rangeEnd the target range to grant on to. [ByteString.EMPTY] means just key, no range.
     * @param callback A [ResponseCallback] to handle the response received.
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleGrantPermissionAsync(role: String,
                                 permType: Permission.Type,
                                 key: ByteString,
                                 rangeEnd: ByteString = ByteString.EMPTY,
                                 callback: ResponseCallback<AuthRoleGrantPermissionResponse>)

    /**
     * Revokes a key or range permission of a specified role (blocking).
     *
     * @param role The name of the role to revoke.
     * @param key The key to revoke.
     * @param rangeEnd The rangeEnd to revoke. [ByteString.EMPTY] means just key, no range.
     * @return [AuthRoleRevokePermissionResponse]
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleRevokePermission(role: String,
                             key: ByteString,
                             rangeEnd: ByteString = ByteString.EMPTY): AuthRoleRevokePermissionResponse

    /**
     * Revokes a key or range permission of a specified role in future.
     *
     * @param role The name of the role to revoke.
     * @param key The key to revoke.
     * @param rangeEnd The rangeEnd to revoke. [ByteString.EMPTY] means just key, no range.
     * @return [ListenableFuture] of [AuthRoleRevokePermissionResponse]
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleRevokePermissionInFuture(role: String,
                                     key: ByteString,
                                     rangeEnd: ByteString = ByteString.EMPTY):
            ListenableFuture<AuthRoleRevokePermissionResponse>

    /**
     * Revokes a key or range permission of a specified role (asynchronously).
     *
     * @param role The name of the role to revoke.
     * @param key The key to revoke.
     * @param rangeEnd The rangeEnd to revoke. [ByteString.EMPTY] means just key, no range.
     * @param callback A [ResponseCallback] to handle the response received.
     * @see [EtcdKVService.get] about [key] and [rangeEnd]
     * @see [xyz.dowenliu.ketcd.option.GetOption.endKey] about [rangeEnd]
     */
    fun roleRevokePermissionAsync(role: String,
                                  key: ByteString,
                                  rangeEnd: ByteString = ByteString.EMPTY,
                                  callback: ResponseCallback<AuthRoleRevokePermissionResponse>)

    /**
     * Deletes a specified role (blocking).
     *
     * @param role The name of the role to delete.
     * @return [AuthRoleDeleteResponse]
     */
    fun roleDelete(role: String): AuthRoleDeleteResponse

    /**
     * Deletes a specified role in future.
     *
     * @param role The name of the role to delete.
     * @return [ListenableFuture] of [AuthRoleDeleteResponse]
     */
    fun roleDeleteInFuture(role: String): ListenableFuture<AuthRoleDeleteResponse>

    /**
     * Deletes a specified role (asynchronously).
     *
     * @param role The name of the role to delete.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun roleDeleteAsync(role: String, callback: ResponseCallback<AuthRoleDeleteResponse>)

    /**
     * Grants a role to a specified user (blocking).
     *
     * @param username The name of the user to grant to.
     * @param role The name of the role to grant.
     * @return [AuthUserGrantRoleResponse]
     */
    fun userGrantRole(username: String, role: String): AuthUserGrantRoleResponse

    /**
     * Grants a role to a specified user in future.
     *
     * @param username The name of the user to grant to.
     * @param role The name of the role to grant.
     * @return [ListenableFuture] of [AuthUserGrantRoleResponse]
     */
    fun userGrantRoleInFuture(username: String, role: String): ListenableFuture<AuthUserGrantRoleResponse>

    /**
     * Grants a role to a specified user (asynchronously).
     *
     * @param username The name of the user to grant to.
     * @param role The name of the role to grant.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userGrantRoleAsync(username: String, role: String, callback: ResponseCallback<AuthUserGrantRoleResponse>)

    /**
     * Revokes a role of specified user (blocking).
     *
     * @param username The name of the user to revoke.
     * @param role The name of the role to revoke.
     * @return [AuthUserRevokeRoleResponse]
     */
    fun userRevokeRole(username: String, role: String): AuthUserRevokeRoleResponse

    /**
     * Revokes a role of specified user in future.
     *
     * @param username The name of the user to revoke.
     * @param role The name of the role to revoke.
     * @return [ListenableFuture] of [AuthUserRevokeRoleResponse]
     */
    fun userRevokeRoleInFuture(username: String, role: String): ListenableFuture<AuthUserRevokeRoleResponse>

    /**
     * Revokes a role of specified user (asynchronously).
     *
     * @param username The name of the user to revoke.
     * @param role The name of the role to revoke.
     * @param callback A [ResponseCallback] to handle the response received.
     */
    fun userRevokeRoleAsync(username: String, role: String, callback: ResponseCallback<AuthUserRevokeRoleResponse>)
}