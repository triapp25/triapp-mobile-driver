package com.triapp.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.triapp.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * Insere uma nova notificação, substituindo se o ID for o mesmo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    /**
     * Retorna um Flow de todas as notificações não lidas para o Tenant atual.
     */
    @Query("SELECT * FROM notifications WHERE tenantId = :tenantId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotificationsByTenant(tenantId: String): Flow<List<NotificationEntity>>

    /**
     * Retorna todas as notificações, lidas e não lidas.
     */
    @Query("SELECT * FROM notifications WHERE tenantId = :tenantId ORDER BY timestamp DESC")
    fun getAllNotificationsByTenant(tenantId: String): Flow<List<NotificationEntity>>

    /**
     * Marca uma notificação específica como lida.
     */
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Long)

    /**
     * Limpa todas as notificações de um Tenant específico.
     */
    @Query("DELETE FROM notifications WHERE tenantId = :tenantId")
    suspend fun clearAll(tenantId: String)
}